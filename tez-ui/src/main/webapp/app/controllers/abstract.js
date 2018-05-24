/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import Ember from 'ember';

import NameMixin from '../mixins/name';
import checkURLIsValid from '../utils/misc';

export default Ember.Controller.extend(NameMixin, {
  // Must be set by inheriting classes
  breadcrumbs: null,

  // Must be set from abstract route
  loadTime: null,
  isLoading: false,
  env: Ember.inject.service("env"),
  helperServerUrl: "helper",
  pollster: Ember.inject.service("active-resource-manager-poller"),
  errorsHandler: Ember.inject.service("errors-handler"),

  init: function () {
    this._super();

    let poll = this.get("pollster");
    let interval = this.get("env.app.healthCheckInterval");
    poll.setInterval(interval);

    Ember.run.later(this, "setBreadcrumbs");
  },

  loaded: Ember.computed("model", "isLoading", function () {
    return this.get("model") && !this.get("isLoading");
  }),

  crumbObserver: Ember.observer("breadcrumbs", function () {
    Ember.run.later(this, "setBreadcrumbs");
  }),

  setBreadcrumbs: function () {
    var crumbs = {},
        name = this.get("name");
    crumbs[name] = this.get("breadcrumbs");
    this.send("setBreadcrumbs", crumbs);
  },

  getActiveRMWebUrl: function (helperServerUrl, callback) {
    $.ajax({
      type: "GET",
      url: helperServerUrl,
      async: false,
      success: function (response) {
        if(typeof callback == "function") {
          callback(response)
        }
      }
    });
  }
});
