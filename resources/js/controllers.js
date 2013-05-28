var IndexCtrl = function($scope, $routeParams) {};

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

var TransactionListCtrl = function($scope, $http, $routeParams) {
    $scope.accountId = $routeParams.accountId;

    $http.get('/api/transaction', {
        params: {
            account_id: $routeParams.accountId,
            something_else: 'asdf'
        }
    }).success(function(data) {
        $scope.transactions = data;
    });

    $scope.$watch('newTansaction', function(newVal, oldVal, scope) {
        console.log('newVal:');
        console.log(newVal);
        console.log('oldVal:');
        console.log(oldVal);
    }, true);
};
