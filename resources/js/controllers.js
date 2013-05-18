var CategoryListCtrl = function($scope, $http) {
    $http.get('/api/category').success(function(data) {
        $scope.categories = data;
    });
};

var AccountListCtrl = function($scope, $http) {
    $http.get('/api/account').success(function(data) {
        $scope.accounts = data;
    });
};
