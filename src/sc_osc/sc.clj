;    Copyright (C) 2023 Joseph Fosco. All Rights Reserved
;
;    This program is free software: you can redistribute it and/or modify
;    it under the terms of the GNU General Public License as published by
;    the Free Software Foundation, either version 3 of the License, or
;    (at your option) any later version.
;
;    This program is distributed in the hope that it will be useful,
;    but WITHOUT ANY WARRANTY; without even the implied warranty of
;    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;    GNU General Public License for more details.
;
;    You should have received a copy of the GNU General Public License
;    along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns sc-osc.sc
  " This is the public interface to the sc-osc library. If access to additional functions
    from the lib directory is required, new functions should be added here."
  (:require [overtone.osc :refer [in-osc-bundle]]  ;; osc-clj lib
            [sc-osc.lib.allocator :refer [alloc-id free-id]]
            [sc-osc.lib.connection :refer [connect connection-status*]]
            [sc-osc.lib.counters :refer [next-id reset-counter!]]
            [sc-osc.lib.event :refer [event on-event
                                      on-sync-event
                                      oneshot-sync-event
                                      remove-event-handler]]
            [sc-osc.lib.lib :refer [deref! uuid]]
            [sc-osc.lib.server-comms :refer [server-osc-peer* server-snd with-server-sync]]
            ))

(defn sc-connect
  ([] (connect))
  ([port] (connect port))
  ([host port] (connect host port))
  )

(defn sc-send-msg
  [& args]
  (apply server-snd args))

(defmacro sc-send-bundle
  [time & msgs]
  `(overtone.osc/in-osc-bundle @server-osc-peer* ~time ~@msgs))

(defn sc-now
  "Return the current time in milliseconds"
  []
  (System/currentTimeMillis))

(defn sc-connection-status
  []
  @connection-status*)

(defn sc-allocate-bus-id
  " Allocates and returns a new bus id for wither a control bus or an audio bus.
    When allocating audio buses, the bus-type should be :audio-bus.
    When allocating a control bus, the bus-type should be :control-bus
    size should be the number of buses to allocate; defaults to 1. If more than 1,
    This will return the id that was allocated or, if more than 1 buse is requested,
    it will return the first id of 'size' number of consecutive ids allocated."
  ([bus-type] (alloc-id bus-type 1 nil))
  ([bus-type size] (alloc-id bus-type size nil))
  ([bus-type size action-fn]
   (alloc-id bus-type size action-fn))
  )

(defn sc-free-id
  [& args]
  (apply free-id args))

(defn sc-with-server-sync
  [& args]
  (apply with-server-sync args))

(defn sc-event
  [& args]
  (apply event args))

(defn sc-on-event
  [& args]
  (apply on-event args))

(defn sc-on-sync-event
  [& args]
  (apply on-sync-event args))

(defn sc-oneshot-sync-event
  [& args]
  (apply oneshot-sync-event args))

(defn sc-remove-event-handler
  [& args]
  (apply remove-event-handler args))

(defn sc-deref!
  [& args]
  (apply deref! args)
  )

(defn sc-next-id
  [& args]
  (apply next-id args))

(defn sc-reset-counter!
  [key]
  (reset-counter! key))

(defn sc-uuid
  []
  (uuid))

(defn sc-debug
  [val]
  (if val
    (do
      (println "Activating detailed OSC debug logs....")
      (sc-osc.lib.event/event-debug-on)
      (overtone.osc/osc-debug true)
      (sc-send-msg "/dumpOSC" 1))
    (do
      (sc-osc.lib.event/event-debug-off)
      (overtone.osc/osc-debug false)
      (sc-send-msg "/dumpOSC" 0))
    ))
