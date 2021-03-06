'use strict';

/* Controllers */

function TreeListCtrl($scope, Tree, $http) {

    $scope.trees = Tree.query();

    $scope.removeTree = function (tree) {
        jConfirm(jQuery.i18n.prop('trees.list.remove.confirm'), jQuery.i18n.prop("main.confirm"), function (val) {
            if (val) {
                tree.$remove(function () {
                    // remove tree from list
                    $scope.trees.removeObject(tree);
                    $scope.trees.removeObject(tree);
                }, function () {
                    motechAlert('trees.list.remove.error', 'main.error');
                });
            }
        });
    };

    $scope.createExample = function () {
        blockUI();
        $http.post('../demo/api/tree/example').
            success(function () {
                $scope.trees = Tree.query();
                unblockUI();
                motechAlert('trees.create.tree.saved', 'main.saved');
            }).error(alertHandler('trees.create.tree.error', 'main.error'));
    };
}

function TreeCreateCtrl($scope, $http) {
    $scope.tree = {
        name: '',
        description: '',
        nodes: [{
            id: 0,
            name: 'root',
            message: '',
            actionsBefore: [],
            actionsAfter: [],
            transitions: []
        }]
    };

    $scope.acyclic = true;
    $scope.checked = false;

    $scope.validationMsgs = new Object();
    $scope.treeNameValidationMsg = '';

    $scope.isChecked = function () {
        return $scope.checked && $scope.acyclic;
    }

    $scope.resetChecked = function () {
        $scope.acyclic = true;
        $scope.checked = false;
    }

    $scope.validation = function () {
        var errors = [], i, j, node, transition;
        $scope.validationMsgs = new Object();
        $scope.treeNameValidationMsg = '';

        if ($scope.tree.name == null || $scope.tree.name == '') {
            var errMsg = jQuery.i18n.prop('trees.error.treeNameRequired')
            $scope.treeNameValidationMsg = errMsg;
            errors.push(errMsg);
        }

        for (i = 0; i < $scope.tree.nodes.length; i += 1) {
            node = $scope.tree.nodes[i];

            if (!node.name) {
                var errMsg = jQuery.i18n.prop('trees.error.nodeNameRequired');
                var errId = node.id + '.name';

                errors.push(node.id + ': ' + errMsg);
                $scope.validationMsgs[errId] = errMsg;
            }

            if (node.message == null || node.message == '') {
                var errMsg = jQuery.i18n.prop('trees.error.nodeMessageRequired')
                var errId = node.id + '.message';

                errors.push(node.id + ': ' + errMsg);
                $scope.validationMsgs[errId] = errMsg;
            }

            for (j = 0; j < node.transitions.length; j += 1) {
                transition = node.transitions[j];

                if (transition.key == null || transition.key == '') {
                    var errMsg =  jQuery.i18n.prop('trees.error.transitionKeyRequired');
                    var errId = node.id + ".transition." + transition.id;

                    errors.push(transition.id + ': ' + errMsg);
                    $scope.validationMsgs[errId] = errMsg;
                }
            }
        }

        return errors;
    }

    $scope.getValidationError = function(nodeId, fieldName, transitionId) {
        var errId = nodeId + '.' + fieldName;
        if (typeof(transitionId) != 'undefined') {
            errId += '.' + transitionId;
        }
        var errMsg = $scope.validationMsgs[errId];
        return (typeof errMsg == 'undefined') ? '' : errMsg;
    }

    $scope.checkTreeCyclic = function () {
        var i, j, id, node, nodes = [];

        for (i = 0; i < $scope.tree.nodes.length; i += 1) {
            for (j = 0; j < $scope.tree.nodes[i].transitions.length; j += 1) {
                id = $scope.tree.nodes[i].transitions[j].node.id;
                node = $scope.tree.nodes[i].transitions[j].node;

                nodes[id] = node;
                $scope.tree.nodes[i].transitions[j].node = node.id;
            }
        }

        $http.post('../demo/api/trees/cycle', $scope.tree).
            success(function (data) {
                $scope.acyclic = data === "false";
                $scope.checked = true;
            });

        for (i = 0; i < $scope.tree.nodes.length; i += 1) {
            for (j = 0; j < $scope.tree.nodes[i].transitions.length; j += 1) {
                id = $scope.tree.nodes[i].transitions[j].node;

                $scope.tree.nodes[i].transitions[j].node = nodes[id];
            }
        }
    }

    $scope.addNode = function () {
        $scope.tree.nodes.push({
            id: $scope.tree.nodes.length,
            name: '',
            message: '',
            actionsBefore: [],
            actionsAfter: [],
            transitions: []
        });
        $scope.resetChecked();
    };

    $scope.removeNode = function (node) {
        var i, j;

        for (i = 0; i < $scope.tree.nodes.length; i += 1) {
            for (j = 0; j < $scope.tree.nodes[i].transitions.length; j += 1) {
                if (node.id == $scope.tree.nodes[i].transitions[j].node.id) {
                    $scope.tree.nodes[i].transitions.remove(j);
                    break;
                }
            }
        }

        $scope.tree.nodes.removeObject(node);
        $scope.resetChecked();
    };

    $scope.addTransition = function (node) {
        node.transitions.push({
            id: node.transitions.length,
            key: '',
            node: $scope.findTransitions(node, false)[0]
        });
        $scope.resetChecked();
    };

    $scope.findTransitions = function (rootNode, checkRootNode) {
        var transitions = [], i, j, node;

        for (i = 0; i < $scope.tree.nodes.length; i += 1) {
            node = $scope.tree.nodes[i];

            if (node.id != 0 && node.id != rootNode.id) {
                transitions.push(node);
            }
        }

        if (checkRootNode) {
            for (i = transitions.length - 1; i >= 0; i -= 1) {
                for (j = 0; j < rootNode.transitions.length; j += 1) {
                    if (rootNode.transitions[j].node != null && transitions[i].id == rootNode.transitions[j].node.id) {
                        transitions.remove(i);
                        break;
                    }
                }
            }
        }

        return transitions;
    };

    $scope.removeTransition = function (node, transition) {
        node.transitions.removeObject(transition);
        $scope.resetChecked();
    };

    $scope.addAction = function (node, eventId, before) {
        if (eventId != null) {
            if (before) {
                node.actionsBefore.push(eventId);
            } else {
                node.actionsAfter.push(eventId);
            }
        }
        $scope.resetChecked();
    }

    $scope.removeAction = function (node, action, before) {
        if (before) {
            node.actionsBefore.removeObject(action);
        } else {
            node.actionsAfter.removeObject(action);
        }
        $scope.resetChecked();
    };

    $scope.sendTree = function () {
        var i, j;

        for (i = 0; i < $scope.tree.nodes.length; i += 1) {
            for (j = 0; j < $scope.tree.nodes[i].transitions.length; j += 1) {
                $scope.tree.nodes[i].transitions[j].node = $scope.tree.nodes[i].transitions[j].node.id;
            }
        }

        blockUI();
        $http.post('../demo/api/trees/create', $scope.tree).
            success(function () {
                var loc = new String(window.location), indexOf = loc.indexOf('#');

                unblockUI();
                window.location = loc.substring(0, indexOf) + "#/trees";
                motechAlert('trees.create.tree.saved', 'main.saved');
            }).error(alertHandler('trees.create.tree.error', 'main.error'));
    };

    $scope.isSaveDisabled = function() {
        return $scope.validation().length == 0 ? false : true;
    };
}

function TreeExecuteCtrl($scope, Tree, $routeParams) {
    $scope.entered = [];
    $scope.history = [];

    $scope.tree = Tree.get({ treeId: $routeParams.treeId }, function () {
        $scope.history.unshift({ response: $scope.tree.nodes[0], alert: 'success' });
    });

    $scope.getEntered = function () {
        return $scope.entered.join('');
    };

    $scope.getMessage = function (response) {
        return response.message || response;
    };

    $scope.addKey = function (key) {
        $scope.entered.push(key);
    };

    $scope.reset = function () {
        $scope.entered = [];
    };

    $scope.send = function () {
        var entered = $scope.getEntered(), found = $scope.tree.nodes[0].transitions.length == 0, i, j, transition, node;

        for (i = 0; i < $scope.tree.nodes[0].transitions.length; i += 1) {
            transition = $scope.tree.nodes[0].transitions[i];

            if (transition.key == entered) {
                for (j = 0; j < $scope.tree.nodes.length; j += 1) {
                    node = $scope.tree.nodes[j];

                    if (node.id == transition.node) {
                        $scope.history.unshift({ request: entered, response: node, alert: 'success' });
                        $scope.tree.nodes[0] = node;
                        $scope.tree.nodes.remove(j);
                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                break;
            }
        }

        if (0 == $scope.tree.nodes[0].transitions.length) {
            $scope.history.unshift({ response: jQuery.i18n.prop('trees.warn.endOfTree'), alert: 'info' });
        }
        if (!found) {
            $scope.history.unshift({ request: entered, response: jQuery.i18n.prop('trees.error.transitionNofFound'), alert: 'error' });
        }

        $scope.reset();

    };

    $scope.restart = function(){
        $scope.entered = [];
        $scope.history = [];

        $scope.tree = Tree.get({ treeId: $routeParams.treeId }, function () {
            $scope.history.unshift({ response: $scope.tree.nodes[0], alert: 'success' });
        });
    }
}

function IVRCallCtrl($scope, i18nService, $http) {

    $http.get('../demo/api/ivrservices').success(function(data) {
        $scope.ivrservices = data;
        $scope.curservice = $scope.ivrservices[0];
    });

    $scope.call = function() {
        $http.get('../demo/api/changeservice?id=' + $scope.curservice.index);

        if ($scope.delay == '0' || $scope.delay == null) {
            $http.get('../demo/api/initiateCall?phone=' + $scope.phone).success(function() {
                alert("making a call now to " + $scope.phone);
            });
        }
        else {
            $http.get('../demo/api/scheduleCall?phone=' + $scope.phone + '&callDelay=' + $scope.delay).success(function() {

                var oldDate = new Date();
                var newDate = new Date(oldDate.getTime() + $scope.delay*60000);

                alert("Scheduled a phone call to " + $scope.phone
                 + " at " + newDate.getHours() + ":" + newDate.getMinutes());
            });
        }
    };
}
