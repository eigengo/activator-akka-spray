var fruitapp = angular.module('fruitapp', ['restangular']);
angular.module('fruitapp').config(function(RestangularProvider){
    RestangularProvider.setBaseUrl('');
});
Object.prototype.getName = function () {
    var prop;
    for (prop in self) {
        if (Object.prototype.hasOwnProperty.call(self, prop) && self[prop] == this && self[prop].constructor == this.constructor) {
            return prop;
        }
    }
    return ""; //no name found
};
