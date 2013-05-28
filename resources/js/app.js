angular.module('hawk', []).config(['$routeProvider', function($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'static/partials/index.html',
            controller: IndexCtrl
        })
        .when('/account', {
            templateUrl: 'static/partials/account-list.html',
            controller: AccountListCtrl
        })
        .when('/account/:accountId', {
            templateUrl: 'static/partials/transaction-list.html',
            controller: TransactionListCtrl
        })
        .otherwise({redirectTo: '/'});
}]);
