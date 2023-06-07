;; copied from the Overtone project overtone.sc.machinery.server.comms


(ns sc-osc.lib.server-comms
  (:use [sc-osc.lib.osc-validator]
        [sc-osc.lib.counters]
        [sc-osc.lib.event]
        )
  (:require
   [sc-osc.lib.lib :refer [uuid deref!]]
   [sc-osc.lib.log :as log]
   )
  )

(defonce osc-debug*       (atom false))
(defonce server-osc-peer* (ref nil))


;; The base handler for receiving osc messages just forwards the message on
;; as an event using the osc path as the event key.
(on-sync-event [:sc-osc :osc-msg-received]
               (fn [{{path :path args :args} :msg}]
                 (when @osc-debug*
                   (println "Receiving: " path args))
                 (event path :path path :args args))
               ::osc-receiver)

(defn- massage-numerical-args
  "Massage numerical args to the form SC would like them. Currently this
  just casts all Longs to Integers and Doubles to Floats."
  [argv]
  (mapv (fn [arg]
          (cond (instance? Long arg)
                (int arg)

                (instance? Double arg)
                (float arg)

                :else
                arg))
        argv))

(defn server-snd
  "Sends an OSC message to the server. If the message path is a known
  scsynth path, then the types of the arguments will be checked
  according to what scsynth is expecting. Automatically converts any
  args which are longs to ints and doubles to floats.

  (server-snd \"/foo\" 1 2.0 \"eggs\")"
  [path & args]
  (let [args (massage-numerical-args (vec args))]
    (log/debug (str "Sending: " path ", args: " (into [] args)))
    (when @osc-debug*
      (println "Sending: " path args))
    (apply validated-snd @server-osc-peer* path args)))

;; (defn on-server-sync
;;   "Registers the handler to be executed when all the osc messages
;;    generated by executing the action-fn have completed. Returns result
;;    of action-fn."
;;   [action-fn handler-fn]
;;   (let [id  (next-id ::server-sync-id)
;;         key (uuid)]
;;     (on-event "/synced"
;;               (fn [msg] (when (= id (first (:args msg)))
;;                          (handler-fn)
;;                          :sc-osc/remove-handler))
;;               key)

;;     (let [res (action-fn)]
;;       (server-snd "/sync" id)
;;       res)))

;; (defn server-sync
;;   "Send a sync message to the server with the specified id. Server will
;;   reply with a synced message when all incoming messages up to the sync
;;   message have been handled. See with-server-sync and on-server-sync for
;;   more typical usage."
;;   [id]
;;   (server-snd "/sync" id))

;; (defn with-server-self-sync
;;   "Blocks the current thread until the action-fn explicitly sends a
;;   server sync.  The action-fn is assumed to have one argument which will
;;   be the unique sync id.  This is useful when the action-fn is itself
;;   asynchronous yet you wish to synchronise with its completion. The
;;   action-fn can sync using the fn server-sync.  Returns the result of
;;   action-fn

;;   Throws an exception if the sync doesn't complete. By specifying an
;;   optional error-msg, you can communicate back to the user through the
;;   timeout exception the cause of the exception. Typical error-msg values
;;   start with \"whilst...\" i.e. \"whilst creating group foo\"."
;;   ([action-fn] (with-server-self-sync action-fn ""))
;;   ([action-fn error-msg]
;;      (let [id   (next-id ::server-sync-id)
;;            prom (promise)
;;            key  (uuid)]
;;        (oneshot-event "/synced"
;;                       (fn [msg] (when (= id (first (:args msg)))
;;                                  (deliver prom true)))
;;                       key)
;;        (let [res (action-fn id)]
;;          (deref! prom (str "attempting to self-synchronise with the server " error-msg))
;;          res))))

(defn with-server-sync
  "Blocks current thread until all osc messages in action-fn have
  completed. Returns result of action-fn.

  Throws an exception if the sync doesn't complete. By specifying an
  optional error-msg, you can communicate back to the user through the
  timeout exception the cause of the exception. Typical error-msg values
  start with \"whilst...\" i.e. \"whilst creating group foo\""
  ([action-fn] (with-server-sync action-fn ""))
  ([action-fn error-msg]
     (let [id   (next-id ::server-sync-id)
           prom (promise)
           key  (uuid)]
       (on-event "/synced"
                 (fn [msg] (when (= id (first (:args msg)))
                            (deliver prom true)
                            :sc-osc/remove-handler))
                 key)
       (let [res (action-fn)]
         (server-snd "/sync" id)
         (deref! prom (str "attempting to synchronise with the server " error-msg))
         res))))

(defn server-recv
  "Register your intent to wait for a message associated with given path
  to be received from the server. Returns a promise that will contain
  the message once it has been received. Does not block current
  thread (this only happens once you try and look inside the promise and
  the reply has not yet been received).

  If an optional matcher-fn is specified, will only deliver the promise
  when the matcher-fn returns true. The matcher-fn should accept one arg
  which is the incoming event info."
  ([path] (server-recv path nil))
  ([path matcher-fn]
     (let [p   (promise)
           key (uuid)]
       (on-sync-event path
                      (fn [info]
                        (when (or (nil? matcher-fn)
                                  (matcher-fn info))
                          (deliver p info)
                          :sc-osc/remove-handler))
                      key)
       p)))
