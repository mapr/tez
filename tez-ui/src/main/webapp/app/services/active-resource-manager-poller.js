/*global more*/
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
import checkURLIsValid from '../utils/misc';

export default Ember.Service.extend({
  env: Ember.inject.service("env"),
  interval: Ember.computed.oneWay("env.app.healthCheckInterval"),
  helperServerUrl: "helper",
  errorsHandler: Ember.inject.service("errors-handler"),


  currentlyExecutedFunction: null,
  init: function() {

    let helperServerUrl = this.get("helperServerUrl");
    var that = this;
    let env = this.get("env");
    this.start(this,function() {
      Ember.$.get(helperServerUrl).then(function(url) {
        url = url.trim();
        if (!checkURLIsValid(url)) {

          let error = {};
          error.message = "YARN ResourceManager (RM) is out of reach.";
          that.set("errorsHandler.error", error);
        } else {
          if(that.get("errorsHandler.error") !== null) {
            that.set("errorsHandler.error", null);
          }
          if (Ember.get(env,"app.hosts.rm") !== url) {
            Ember.set(env, "app.hosts.rm", url);
          }
        }
      });

    });
  },

  start: function(context, pollingFunction) {
    this.set('currentlyExecutedFunction', this._schedule(context, pollingFunction, [].slice.call(arguments, 2)));
  },

  stop: function() {
    Ember.run.cancel(this.get('currentlyExecutedFunction'));
  },

  _schedule: function(context, func, args) {
    return Ember.run.later(this, function() {
      this.set('currentlyExecutedFunction', this._schedule(context, func, args));
      func.apply(context, args);
    }, this.get('interval'));
  },

  setInterval: function(interval) {
    this.set('interval', interval);
  }
});
