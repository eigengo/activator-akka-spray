#Akka and Spray

#Akka and Spray
This application shows how to build Akka application with Spray API. In this tutorial, I am going to use Spray's ``spray-can``, ``spray-httpx``, ``spray-json`` artefacts with the _Akka_ artefacts to build an application that receives HTTP requests with JSON payloads, unmarshals the JSON into instances of our own classes (``case class``es, to be exact). It then sends these instances to the appropriate `Actor`s for processing. When the actors reply, it marshals the responses into JSON, and uses that to construct the HTTP responses.

I shall also explore the _Cake pattern_, which enable us to separate out parts of the system so that I can "assemble" the parts of the cake into the components that I ultimately run or test.

#Akka and Spray
Let me begin by showing the overall structure of the application we are building.

![Overall structure](/overall.png)

In this template & tutorial, you will learn how to construct [Akka](http://akka.io)-based applications; how to test them (using [TestKit](http://doc.akka.io/docs/akka/snapshot/scala/testing.html) and [Specs2](http://etorreborre.github.io/specs2/); and how to provide RESTful HTTP API using [Spray](http://spray.io/).

#The core
I begin by constructing the core of our system. It contains the top-level ``MessengerActor`` and ``RegistrationActor``. The ``MessengerActor`` contains two child actors, the ``EmailActor`` and the `SMSActor`.

The top-level actors _live_ in the ``CoreActors`` trait. This trait's [self-type annotation](http://docs.scala-lang.org/glossary/) defines that instances that mix in this trait must also mix in some subtype of the ``Core`` trait.

This is a rather long description of the first two lines of the ``CoreActors`` trait.


```
trait CoreActors {
  this: Core =>

}
```

Because of the self-type declaration, I have access to all members of the ``Core`` trait in the ``CoreActors`` trait. Our ``Core`` trait defines only one member, namely ``implicit def system: ActorSystem``; and I use the ``system`` to create the top-level actors.

The ``CoreActor`` in its entirety is therefore

```
trait CoreActors {
  this: Core =>

  val registration = system.actorOf(Props[RegistrationActor])
  val messenger    = system.actorOf(Props[MessengerActor])

}
```

I also provide the implementation of the ``Core`` trait that actually constructs the ``ActorSystem``. I call this trait ``BootedCore``. It instantiates the ``ActorSystem`` and registers the JVM termination hook to shutdown the ``ActorSystem`` on JVM exit.

```
trait BootedCore extends Core {

  implicit val system = ActorSystem("akka-spray")

  sys.addShutdownHook(system.shutdown())

}
```
As you can see, the ``BootedCore`` is a straight-forward implementation of the ``Core`` trait. If I now wanted, I could write a simple application that starts our actors by mixing in the traits.

```
object Cli extends App with BootedCore with CoreActors
```
I must be mindful of the initialization order, though. It would be an error to write ``object Cli extends App with CoreActors with BootedCore``!

It is because the ``BootedCore``'s implementation of the ``system`` method is ``implicit val system = ActorSystem("akka-spray")`` and if I write ``object Cli extends App with CoreActors with BootedCore``, the code in ``CoreActors`` runs before the code in ``BootedCore``--meaning that when the code in ``CoreActors`` runs, the value of the ``system`` ``val`` is still ``null``.

Having to worry about the order in which I mix in the traits is wholly unsatisfactory. I need to find a way in which a `val` can be evaluated immediately when its value is required, but once it is evaluated, it behaves like an ordinary `val`. Rephrasing, I want a member whose value will be computed and remembered on first access, and
subsequent accesses will return the remembered value.

To do this in Scala, I mark the ``val`` as ``_lazy_``. Thus, our ``BootedCore``'s ``system`` becomes

```
trait BootedCore extends Core {

  implicit lazy val system = ActorSystem("akka-spray")

  sys.addShutdownHook(system.shutdown())

}
```

Notice the ``lazy`` keyword--now I am free to mix in the required traits in any order.

#Testing

Let's now explore why I did so much code gymnastics. I could have just as easily defined the ``CoreActors`` trait to contain all the code in ``Core`` and ``BootedCore``.

The motivation was testing. When I am using Akka's [TestKit](http://doc.akka.io/docs/akka/snapshot/scala/testing.html), we construct a test-only ``ActorSystem``. This testing ``ActorSystem`` allows us to test the actors as if the messages sent and received were method calls. In other words, the ``ActorSystem`` in our tests uses the
``CallingThreadDispatcher``, making the message delivery synchronous.

As it happens, the ``TestKit`` contains a member called ``system: ActorSystem``; and it matches the ``Core`` trait. Therefore, it is easy to make our _specification_ implement the ``Core`` trait. This in turn means that our test satisfies the self-type annotation of the ``CoreActors`` trait.

And thus, I can easily write a test for our entire application's structure.

```
class RegistrationActorSpec extends TestKit(ActorSystem()) with SpecificationLike with CoreActors with Core {
  ...
}
```

I extend the ``TestKit`` class, which gives us access to all the underlying Akka's test mechanisms; I also mix in [Specs2](http://etorreborre.github.io/specs2/)'s ``SpecificationLike``, which gives us the convenient DSL for writing our test scenarios and assertions. Finally, because I am writing code to test _my_ actors, I mix in the ``CoreActors`` and ``Core`` traits.
I do not need to do any more work, because the member ``system: ActorSystem`` fully implements the ``Core`` trait. However, I need to implement the ``Core`` trait to satisfy the self-type declaration of the ``CoreActors``.

I could have defined ``Core`` to be [structural type](http://docs.scala-lang.org/glossary/), in which case,
I would not have to worry about implementing `Core` here. If you want to try it out, remove the ``trait Core { ... }`` and replace it with

```
package object core {

  type Core = { def system: ActorSystem }

}
```

Here, the ``Core`` type is a _structural type_, which says that ``Core`` is anything which contains the ``system: ActorSystem`` member.

#The actors

The implementation of the actors is not particularly interesting. I will explore the common patterns of naming and structuring that I believe are useful; the implementation of the actors I leave to the readers.

I begin with the ``RegistrationActor``. Its purpose is to register the users in our system. The actor's [companion object](http://docs.scala-lang.org/glossary/) holds all the messages that the actor deals with. In code, the structure is

```
object RegistrationActor {
  case class Register(user: User)
  case object Registered
  case object NotRegistered
}

class RegistrationActor extends Actor {
  import RegistrationActor._

  def receive: Receive = ???
}
```

This structure allows me to tidy up the messages and perhaps some utility functions in the future from the actor itself.

Onwards to the actor's implementation, then. As I said, it will be rather trivial, mainly demonstrating approaches &amp; patterns. When it receives the ``Register`` message, it performs the required processing and then replies to the _sender_ with the outcome of the registration process.

```
class RegistrationActor extends Actor{
import RegistrationActor._

  def receive: Receive = {
    case Register(user) if user.email.isEmpty => sender ! Left(NotRegistered)
    case Register(user)                       => sender ! Right(Registered)
  }

}
```

I warned you, it is not very clever at all :).

#Testing the ``RegistrationActor``

As simple as the ``RegistrationActor`` is, I can (and _should_) still test it.
I can either test the actor in isolation, writing a _unit test_, or I can test our
entire application, but focusing on the ``RegistrationActor``--an _integration test_.

I will show the integration test approach here.

To allow us to examine what happens in our actors, I need to use special--_crippled_--
``ActorSystem`` that processes the messages synchronously. To the code, then. We
extend the ``TestKit`` class and mix in the required traits to construct the test.

```
class RegistrationActorSpec extends TestKit(ActorSystem()) with SpecificationLike with CoreActors with Core {
  sequential

  private def mkUser(email: String): User = User(UUID.randomUUID(), "A", "B", email)

  "Registration should" >> {

    "reject invalid email" in {
      registration ! Register(mkUser(""))
      expectMsg(Left(NotRegistered))
      success
    }

    "accept valid user to be registered" in {
      registration ! Register(mkUser("jan@eigengo.com"))
      expectMsg(Right(Registered))
      success
    }
  }

}
```

This is the entire body of our test. I instantiate the actors in ``CoreActors``, satisfying the ``Core`` self-type annotation by having the ``RegistrationActorSpec`` implement the ``Core`` trait. (Remember, I can do that just by writing ``with Core``, because ``TestKit`` already contains the member ``system: ActorSystem``, which is
all that is needed to fully implement ``Core``.)

Onwards. If you run the test, it will fail. It will complain about failing timeouts, namely
```
assertion failed: timeout (3 seconds) during expectMsgClass waiting for class core.RegistrationActor$NotRegistered$
```

And yet, if you place a breakpoint in the ``RegistrationActor``'s ``receive``, you will see that it is indeed executing; replying to the ``sender`` with the appropriate message.

The only conclusion is that the ``sender`` itself is somehow broken. Indeed. Going back to our test, I write ``registration ! Register(mkUser(""))``. If you explore the ``!`` function, you will see that it is a curried function, whose first parameter list is the message to be sent, and its second parameter list is the ``ActorRef`` which represents the sender. It is marked as ``implicit``, but it also contains a _default value_.
So, if no ``ActorRef`` instance is available implicitly, the default value will be used instead. Unfortunately for me here, the default value is ``Actor.noSender``.

To fix the problem, all I need to do is have an implicit ``ActorRef`` value. However, this ``ActorRef`` should also somehow interact with the rest of the code in TestKit. Furtunately for the lazy, TestKit provides the ``ImplicitSender`` trait, which makes the ``testActor`` implicitly available; and the ``testActor`` interacts with
all the ``expect...`` functions in TestKit.

```
class RegistrationActorSpec extends TestKit(ActorSystem()) 
  with SpecificationLike with CoreActors with Core with ImplicitSender {
   ...
}
```

I mix in the `ImplicitSender` and all is good!

#Spray

I am going to use most of the Spray components, namely

*   `spray-io` for the low-level, asynchronous I/O,
*   `spray-http` for the HTTP protocol implementation on top of `spray-io`,
*   `spray-routing`, which provides convenient DSL for mapping HTTP requests to functions,
*   `spray-httpx` for the unmarshalling and marshalling of the HTTP requests and responses,
*   `spray-json` for JSON marshallers and unmarshallers

This feels like quite a handful, but you will see that the code is rather succinct and, of course, type safe. Even more importantly, the Spray code does not interfere with the core of our system. The core can remain completely oblivious of the way in which it is exposed.

I continue to strictly separate the layers of (even if so trivial) application; therefore, I will separate the REST API from the code that starts the HTTP server hosting the API. In keeping with the rest of the system, I will split the layers into traits, giving me the ``Api`` and ``Web`` traits. As you can guess, the ``Api`` trait
contains just the REST API, and the ``Web`` trait exposes the APIs in a real
HTTP server.

To make the ``Api`` trait work with the rest of the system, I will use the self-type annotation and require that the ``Api`` trait is mixed in with ``CoreActors`` and ``Core``. The ``Web`` trait will need to be mixed in with ``Api``.

```
trait Api {
  this: CoreActors with Core =>

  ...
}
```

And

```
trait Web {
  this: Api with CoreActors with Core =>

  ...
}
```

All of this to enable me to write tests that exercise just the REST API, without the need to start the HTTP server and deal with the added complexity of real HTTP requests; and to allow me to write a subtype of `App` that combines all the components and starts a real HTTP server; server, which hosts the API, which in turn uses the core actors to do the heavy processing. In code, the _entire_ application is just one line:

```
object Rest extends App with BootedCore with CoreActors with Api with Web
```

_Amazing!_

#The API

Let's explore the ``Api`` trait, which defines the REST endpoints. In keeping with the structure from the diagram, I have kept each endpoint in its own class. The ``Api`` trait constructs the classes for these endpoints and then concatenates the routes they each expose. The ``RoutedHttpService`` then routes the incoming HTTP requests accordingly.

```
trait Api extends RouteConcatenation {
  this: CoreActors with Core =>

  private implicit val _ = system.dispatcher

  val routes =
    new RegistrationService(registration).route ~
    new MessengerService(messenger).route

  val rootService = system.actorOf(Props(new RoutedHttpService(routes)))

}
```

I instantiate the ``RegistrationService`` and the ``MessengerService``, giving each reference to the approprite ``ActorRef`` from the ``CoreActors``. (I can do this, because the ``Api`` trait declares the ``CoreActors`` as its self-type.)

#Registration Service

I will show the code and structure of the ``RegistrationService``, which is ever so slightly more complex than the code in ``MessengerService``. The motivation for the ``RegistrationService`` is to have REST API that receives JSON payloads in that can be mapped to instances of the ``Register`` case class.

```
{ "user": {
    "id": "122fa630-92fd-11e2-9e96-0800200c9a66",
    "firstName":"Jan",
    "lastName":"Machacek",
    "email":"jan@eigengo.com" } }
```
I expect replies to match the responses (``Either[NotRegistered, Registered]``); the value on the left projection should result in HTTP status _bad request_; the value on the right projection should be HTTP OK. In our application, the value ``Left(NotRegistered)`` should be represented as HTTP status ``400`` with JSON payload
```
{ "value": "NotRegistered" }
```

The ``Right(Registered)`` should be represented as HTTP status ``200`` with JSON payload
```
{ "value": "Registered" }
```

Before I begin to worry about the marshalling and unmarshalling, let's deal with the actual HTTP requests. _Spray-can_ deals with the low-level I/O of asynchronous HTTP (and SPDY!) server; it then turns the HTTP requests and responses into ``HttpRequest`` messages; the response sent to the sender is the ``HttpResponse``, containing the response entity, headers, etc.

_Spray-http_ deals with convenient routing so that I don't have to implement actors that receive ``HttpRequest``s and reply to the sender with the raw ``HttpResponse``s; _spray-httpx_ adds the marshalling and unmarshalling support.

![Spray](/spray.png)

Let's start working on the ``RegistrationService``. I will begin by using the routes.

```
class RegistrationService(registration: ActorRef)
                         (implicit executionContext: ExecutionContext)
  extends Directives {

  val route =
    path("register") {
      post {
        complete {
          "OK"
        }
      }
    }

}
```
This is a good start. It clearly demonstrates the ease of Spray's routing. I say that on _path_
``register``, on HTTP method _POST_, I _complete_ the request with body ``"OK"``. Unfortunately, the string ``"OK"`` doesn't quite meet our requirement. To do so, I need to interpret the request as instance of ``Register`` and turn our ``Either[NotRegistered.type, Registered.type]`` into the response.

All of this requires some marshalling jiggery pokery. Firstly, I need to be able to turn the HTTP requests into instances of our types and then I need to be able to do the reverse. Spray provides pluggable marshalling mechanism to do that. The instances of the typeclasses ``Marshaller[A]`` and ``Unmarshaller[A]`` are responsible for performing the _request entity_ -&gt; _our instance_ and _our instance_ -&gt; _response entity_ transformations.

I will not get into a complex discussion of typeclasses; I will just say that typeclass defines behaviour for a certain type and a typeclass instance is the implementation of such behaviour. Spray includes the ``spray-json`` library, which, together with the ``SprayJsonSupport`` trait allows me to wire in ``spray-json`` marshallers and unmarshallers and use them in my Spray code.

Without further delay, let me show you how it's done in code and then explore the details.

```
class RegistrationService(registration: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {

  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)

  implicit val userFormat = jsonFormat4(User)
  implicit val registerFormat = jsonFormat1(Register)
  implicit val registeredFormat = jsonObjectFormat[Registered.type]
  implicit val notRegisteredFormat = jsonObjectFormat[NotRegistered.type]
  implicit object EitherErrorSelector extends 
    ErrorSelector[NotRegistered.type] {
    def apply(v: NotRegistered.type): StatusCode = StatusCodes.BadRequest
  }

  val route =
    path("register") {
      post {
        handleWith { ru: Register =>
          (registration ? ru).mapTo[Either[NotRegistered.type, Registered.type]]
        }
      }
    }

}
```

Looking at the ``route`` definition, the only difference is that I changed ``complete`` to ``handleWith``; and then wired in the call to our ``RegistrationActor``.

Reading the code intuitively, I say that to handle requests to _register_ on HTTP method _post_, I turn the request into ``Register`` and then _handleWith_ a function that returns a ``Future[Either[...]]``, which we
turn into a response.

Intuition did not fail us. All I have to do is to implement the behaviour of turning the requests into our types and our types into responses. So, instances of typeclasses! Those are the mysterious lines

```
implicit val userFormat = jsonFormat4(User)
implicit val registerFormat = jsonFormat1(Register)
implicit val registeredFormat = jsonObjectFormat[Registered.type]
implicit val notRegisteredFormat = jsonObjectFormat[NotRegistered.type]
```

You see, their types are ``RootJsonFormat[A]``, where ``A`` is ``User``, ``Register``, and so on. ``RootJsonFormat[A]`` contains functions that read JSON and write JSON; ``SprayJsonSupport`` then contains further typeclass instances that implement ``Marshaller[A]`` given ``JsonWriter[A]``, and ``Unmarshaller[A]`` given
``JsonReader[A]``. Moving on, I can create other interesting marshalling typeclasses.

For example, if I know how to marshal some type ``A``, I also know how to marshal ``Seq[A]``, ``Future[A]``; if I know how to marshal some ``A`` and ``B``, I can marshal ``Either[A, B]``, and so on. I am now ready to write
a marshaller that marshals ``Either[A, B]`` and, for the left values, it also indicates the corret HTTP status code. Its signature is rather scary

```
type ErrorSelector[A] = A => StatusCode

implicit def errorSelectingEitherMarshaller[A, B]
  (implicit ma: Marshaller[A], mb: Marshaller[B], esa: ErrorSelector[A]): 
  Marshaller[Either[A, B]] =

  Marshaller[Either[A, B]] { (value, ctx) =>
    value match {
      case Left(a) =>
        val mc = new CollectingMarshallingContext()
        ma(a, mc)
        ctx.handleError(ErrorResponseException(esa(a), mc.entity))
      case Right(b) =>
        mb(b, ctx)
    }
  }
```

And breathe! Now, I have a function ``errorSelectingEitherMarshaller[A, B]`` that returns ``Marshalle[Either[A, B]]``. To be able to do that, it must know how to marshal ``A`` and ``B``; and if I want to be able to indicate the status code, I must also be able to turn ``A``s into ``StatusCode``. That's what the three implicit parameters do: I am asking the compiler to implicitly find an instance of ``Marshaller[A]``, ``Marshaller[B]`` and ``ErrorSelector[A]``; in other words, instances of the ``Marshaller`` and ``ErrorSelector`` typeclasses for instances ``A`` and ``B``.

To use, I must give the typeclass instance for ``ErrorSelector[A]``, where ``A`` is ``NotRegistered.type``;
I do so by defining a singleton:

```
implicit object EitherErrorSelector extends ErrorSelector[NotRegistered.type] {
  def apply(v: NotRegistered.type): StatusCode = StatusCodes.BadRequest
}
```

So, any ``NotRegistered`` value is ``StatusCodes.BadRequest``.

#The web server

To complete the picture, I must implement the ``Web`` trait, which takes the ``rootService`` in the ``Api`` trait and hosts it in the ``spray-can`` HTTP server. The good news is that the code gets simpler:

```
trait Web {
  this: Api with CoreActors with Core =>

  IO(Http)(system) ! Http.Bind(rootService, "0.0.0.0", port = 8080)

}
```

I create the ``Http`` extension, pass it to Akka's ``IO`` machinery; and finally send it the ``Http.Bind`` message to bind the ``rootService`` to all interfaces, on port ``8080``.

#The Scala server

I am excited to present the final application that combines all our components in an HTTP server. It is
```
object Rest extends App with BootedCore with CoreActors with Api with Web
```
That's all there is to it. I have created an ``App``, mixed in all required traits and we're good to go.

#AngularJS Upload
Let's not stop here, though. Today's users expect good client applications, too. Note that, as usual, the Akka code does nothing special with the uploaded files; it simply demonstrates that we can acccept ``multipart/form-data``-encoded POST.

![Screenshot](/upload.png)

This is our AngularJS application in the browser. We press the _Add files..._ button to add (images) that will be uploaded. When the users click the _Start upload_ button, we start the uploads in parallel, but each POST contains one file. The _Cancel upload_ button clears the form.

#Spray code
The requests are going to ``register/image``, so we need to add the appropriate _handler_ to the ``RegistrationService`` to handle POST to that path, to unmarshal the request as ``MultipartFormData``. We then ``complete`` the request with the appropriate response.

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
        handleWith { ru: Register => 
          (registration ? ru).mapTo[
            Either[NotRegistered.type, Registered.type]] 
        }
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

This is the entire source code of the ``RegistrationService`` to show you where the new code fits in. The new bit is the usage of ``entity(as[A]) { a => ... }``, which takes the posted ``HttpEntity``, finds the instance of the ``Unmarshaller`` typeclass for the type ``A``. When the unmarshalling succeeds, it applies the given function ``{ a => ... }`` to complete the request. In our case, we have

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

This means that on POST to ``register/image``, we unmarshal the entity as ``MultipartFormData``
and apply the function:

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

To the successfully unmarshalled value. In that function, we ``complete`` the request by--ultimately--returning either ``s"""{"size":$size}"""`` or ``"""{"size":0}"""``.

![Oh the humanity](/hindenburg.png)

##Stringly-typed!
Hand-constructing JSON, XML, ..., anyting really, is a _terrible idea_. Let's get rid of those ``s"""{"size":$size}"""`` and ``"""{"size":0}"""`` strings and replace them with a convenient case class.

```
case class ImageUploaded(size: Int)
```

We can easily define the ``JsonWriter`` typeclass instance for the ``ImageUploaded`` type by adding an implicit value of type ``JsonWriter[ImageUploaded]``; and this is exactly what the ``jsonFormat`` functions do.

```
implicit val imageUploadedFormat = jsonFormat1(ImageUploaded)
```

##Out with Strings
So, we add the case class and the ``JsonWriter[ImageUpload]`` typeclass instance, which allows us to get rid of the ``String``s in the ``complete`` function.

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

Now, this is better: there are no ``String``s and we handle the file uploads quite nicely. Unfortunately, there are still _too many notes_. Dissecting the ``post`` handler, we have:

```scala
entity(as[MultipartFormData]) { data =>
  complete {
    ImageUploaded(...)
  }
}
```

The ``entity(as[A]) { a: A => complete { ... } }`` can be replaced with ``handleWith``: the same code we use in the ``register`` posts. And so, the final code is simply:


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
Now that we have the Spray application sorted out, let's tackle the JavaScript app. Its source is in ``src/main/angular``. The main components are ``index.html`` and ``js/app.js``. One cannot just open the ``index.html`` _file_ in the browser. The other resources (JavaScripts, stylesheets) will not load properly, but even if they did, the browser would refuse to call our Spray server, because the locations do not match. Our Spray server runs on ``http://localhost:8080``, but the AngularJS application's location would be ``file:///.../index.html``.

To allow us to test it, we need to (easily) serve the AngularJS application _as well as_ our Spray
application on the same location. In this post, I will only tackle the development setup, leaving me space to post about the _proper_ AWS setup in the future.

##Development setup
Let's use Apache to serve the AngularJS application at ``http://localhost/~USER/angular`` and let's host the Spray application (or, in fact, anything that listens on port ``8080``) at ``http://localhost/~USER/app``.

To make this happen, we'll enable Apache's per-user home pages and we'll drop in ``ProxyPass`` and ``ProxyPassReverse`` directives. On my machine, the configuration for ``$USER`` lives in ``/etc/apache2/users/$USER.conf``.

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

Of course, you cannot use ``$USER``, you must replace it with your real username! I don't really want to _copy_ the ``src/main/angular`` directory to the ``~/Sites`` directory, so I've added the ``+FollowSymLinks`` directive and created a symbolic link in ``~/Sites`` to point to wherever I have ``src/main/angular``. In my case, the listing of ``~/Sites`` is

```bash
~/Sites$ ls -la
total 16
lrwxr-xr-x  ... angular -> /.../akka-spray/src/main/angular
-rw-r--r--  ... index.html
```

Now, after adding this file and starting (or restarting) Apache ``sudo apachectl start`` (or ``sudo apachectl restart``), you are ready to see the app by going to ``http://localhost/~USER/angular``.

So, you are now ready to go: opening ``http://localhost/~USER/angular`` shows the AngularJS application, where you can add as many files as you like, and clicking the _Start upload_ button sends the files to the Spray application. The Spray application unceremoniously prints the file size: I shall leave some interesting processing logic as exercise for the readers.

As usual, the source code is at [https://github.com/eigengo/activator-akka-spray](https://github.com/eigengo/activator-akka-spray); feel free to report issues, or to send your contributions!