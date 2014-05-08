angular.module('fruitapp').controller('fruits-controller', function($scope, Restangular) {
    $scope.p = Restangular.all('fruits').getList().then(function(fruitlist) {
        $scope.fruitlist = fruitlist
    });
});