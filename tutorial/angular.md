#AngularJS Upload

#AngularJS Upload
This post follows up on the Akka and Spray post, and it extends it by adding a file upload handler. This
allows us to upload images to the user's account. (Note that, as usual, the Akka code does nothing special
with the uploaded files; it simply demonstrates that we can acccept ``multipart/form-data``-encoded POST.)

![Screenshot](/upload.png)

This is our AngularJS application in the browser. We press the _Add files..._ button to add (images) that
will be uploaded. When the users click the _Start upload_ button, we start the uploads in parallel,
but each POST contains one file. The _Cancel upload_ button clears the form.

#Spray code
The requests are going to ``register/image``, so we need to add the appropriate "handler" to the
``RegistrationService`` to handle POST to that path, to unmarshal the request as ``MultipartFormData``.
We then ``complete`` the request with appropriate response.

```scala
class RegistrationService(registration: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {

  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)

  implicit val userFormat = jsonFormat4(User)
  implicit val registerFormat = jsonFormat1(Register)
  implicit val registeredFormat = jsonObjectFormat[Registered.type]
  implicit val notRegisteredFormat = jsonObjectFormat[NotRegistered.type]

  implicit object EitherErrorSelector extends ErrorSelector[NotRegistered.type] {
    def apply(v: NotRegistered.type): StatusCode = StatusCodes.BadRequest
  }

  val route =
    path("register") {
      post {
        handleWith { ru: Register => (registration ? ru).mapTo[Either[NotRegistered.type, Registered.type]] }
      }
    } ~
    path("register" / "image") {
      post {
        entity(as[MultipartFormData]) { data =>
          complete {
            data.fields.get("files[]") match {
              case Some(imageEntity) =>
                val size = imageEntity.entity.buffer.length
                println(s"Uploaded $size")
                "OK"
              case None =>
                println("No files")
                "Not OK"
            }
          }
        }
      }
    }

}
```

This is the entire source code of the ``RegistrationService`` to show you where the new code fits in.
The new bit is the usage of ``entity(as[A]) { a => ... }``, which takes the posted ``HttpEntity``,
finds the instance of the ``Unmarshaller`` typeclass for the type ``A``. When the unmarshalling succeeds,
it applies the given function ``{ a => ... }`` to complete the request. In our case, we have

```scala
path("register" / "image") {
  post {
    entity(as[MultipartFormData]) { data =>
      complete {
        data.fields.get("files[]") match {
          case Some(imageEntity) =>
            val size = imageEntity.entity.buffer.length
            println(s"Uploaded $size")
            "OK"
          case None =>
            println("No files")
            "Not OK"
        }
      }
    }
  }
}
```

This means that on POST to ``register/image``, we unmarshal the entity as ``MultipartFormData`` and apply
the function

```scala
{ data =>
  complete {
    data.fields.get("files[]") match {
      case Some(imageEntity) =>
        val size = imageEntity.entity.buffer.length
        println(s"Uploaded $size")
        s"""{"size":$size}"""
      case None =>
        println("No files")
        """{"size":0}"""
    }
  }
}
```

To the successfully unmarshalled value. In that function, we ``complete`` the request by--ultimately--
returning either ``s"""{"size":$size}"""`` or ``"""{"size":0}"""``.

![Oh the humanity](/hindenburg.png)

##Stringly-typed!
Hand-constructing JSON, XML, ..., anyting really, is a _terrible idea_. Let's get rid of those
``s"""{"size":$size}"""`` and ``"""{"size":0}"""`` strings and replace them with a convenient
case class.

```
case class ImageUploaded(size: Int)
```

We can easily define the ``JsonWriter`` typeclass instance for the ``ImageUploaded`` type by adding
an implicit value of type ``JsonWriter[ImageUploaded]``; and this is exactly what the ``jsonFormat``
functions do.

```
implicit val imageUploadedFormat = jsonFormat1(ImageUploaded)
```

##Out with Strings
So, we add the case class and the ``JsonWriter[ImageUpload]`` typeclass instance, which allows us to
get rid of the ``String``s in the ``complete`` function.

```scala
class RegistrationService(registration: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {

  case class ImageUploaded(size: Int)
  implicit val imageUploadedFormat = jsonFormat1(ImageUploaded)

  ...

  val route =
    ...
    path("register" / "image") {
      post {
        entity(as[MultipartFormData]) { data =>
          complete {
            data.fields.get("files[]") match {
              case Some(imageEntity) =>
                val size = imageEntity.entity.buffer.length
                println(s"Uploaded $size")
                ImageUploaded(size)
              case None =>
                println("No files")
                ImageUploaded(0)
            }
          }
        }
      }
    }

}
```

Now, this is better: there are no ``String``s and we handle the file uploads quite nicely. Unfortunately,
there are still _too many notes_. Dissecting the ``post`` handler, we have:

```scala
entity(as[MultipartFormData]) { data =>
  complete {
    ImageUploaded(...)
  }
}
```

The ``entity(as[A]) { a: A => complete { ... } }`` can be replaced with ``handleWith``: the same code
we use in the ``register`` posts. And so, the final code is simply:


```scala
path("register" / "image") {
  post {
    handleWith { data: MultipartFormData =>
      data.fields.get("files[]") match {
        case Some(imageEntity) =>
          val size = imageEntity.entity.buffer.length
          println(s"Uploaded $size")
          ImageUploaded(size)
        case None =>
          println("No files")
          ImageUploaded(0)
      }
    }
  }
}
```

#Running the AngularJS application
Now that we have the Spray application sorted out, let's tackle the JavaScript app. Its source is in
``src/main/angular``. The main components are ``index.html`` and ``js/app.js``. One cannot just open
the ``index.html`` _file_ in the browser. The other resources (JavaScripts, stylesheets) will not load
properly, but even if they did, the browser would refuse to call our Spray server, because the locations
do not match. Our Spray server runs on ``http://localhost:8080``, but the AngularJS application's
location would be ``file:///.../index.html``.

To allow us to test it, we need to (easily) serve the AngularJS application _as well as_ our Spray
application on the same location. In this post, I will only tackle the development setup, leaving me
space to post about the _proper_ AWS setup in the future.

##Development setup
Let's use Apache to serve the AngularJS application at ``http://localhost/~USER/angular`` and let's
host the Spray application (or, in fact, anything that listens on port ``8080``) at
``http://localhost/~USER/app``.

To make this happen, we'll enable Apache's per-user home pages and we'll drop in ``ProxyPass`` and
``ProxyPassReverse`` directives. On my machine, the configuration for ``$USER`` lives
in ``/etc/apache2/users/$USER.conf``.

```xml
<Directory "/Users/$USER/Sites/">
	Options Indexes Multiviews +FollowSymLinks
	AllowOverride AuthConfig Limit
	Order allow,deny
	Allow from all
</Directory>

ProxyPass /~$USER/app/ http://localhost:8080/
ProxyPassReverse /~$USER/app/ http://localhost:8080/
```

Of course, you cannot use ``$USER``, you must replace it with your real username! I don't really want
to _copy_ the ``src/main/angular`` directory to the ``~/Sites`` directory, so I've added the
``+FollowSymLinks`` directive and created a symbolic link in ``~/Sites`` to point to wherever I have
``src/main/angular``. In my case, the listing of ``~/Sites`` is

```bash
~/Sites$ ls -la
total 16
lrwxr-xr-x  ... angular -> /.../akka-spray/src/main/angular
-rw-r--r--  ... index.html
```

Now, after adding this file and starting (or restarting) Apache ``sudo apachectl start`` (or ``sudo apachectl restart``),
you are ready to see the app by going to ``http://localhost/~USER/angular``.

So, you are now ready to go: opening ``http://localhost/~USER/angular`` shows the AngularJS application,
where you can add as many files as you like, and clicking the _Start upload_ button sends the files to the
Spray application. The Spray application unceremoniously prints the file size: I shall leave some interesting processing
logic as exercise for the readers.

As usual, the source code is at [https://github.com/eigengo/activator-akka-spray](https://github.com/eigengo/activator-akka-spray);
feel free to report issues, or to send your contributions!