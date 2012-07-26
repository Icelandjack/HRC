(ns hrc.core
  (:import (java.net InetAddress ServerSocket Socket
                     SocketException)
           (java.io InputStreamReader OutputStream
                    OutputStreamWriter PrintWriter BufferedReader))
  (:use [clojure.string :only [split]]))

(defn- on-thread [f]
  (doto (Thread. ^Runnable f)
    (.start)))

(defn- close-socket [^Socket s]
  (when-not (.isClosed s)
    (doto s
      (.shutdownInput)
      (.shutdownOutput)
      (.close))))

(defn- accept-fn [^Socket s connections fun]
  (let [ins (.getInputStream s)
        outs (.getOutputStream s)]
    (on-thread #(do
                  (dosync (commute connections conj s))
                  (try
                    (fun ins outs)
                    (catch SocketException e))
                  (close-socket s)
                  (dosync (commute connections disj s))))))

(defstruct server-def :server-socket :connections)

(defn- create-server-aux [fun ^ServerSocket ss]
  (let [connections (ref #{})]
    (on-thread #(when-not (.isClosed ss)
                  (try
                    (accept-fn (.accept ss) connections fun)
                    (catch SocketException e))
                  (recur)))
    (struct-map server-def :server-socket ss :connections connections)))

(defn create-server
  "Creates a server socket on port. Upon accept, a new thread is
  created which calls:

  (fun input-stream output-stream)

  Optional arguments support specifying a listen backlog and binding
  to a specific endpoint."
  ([port fun backlog ^InetAddress bind-addr]
     (create-server-aux fun (ServerSocket. port backlog bind-addr))) ;; new ServerSover(port, backlog, bind-addr)
  ([port fun backlog]
     (create-server-aux fun (ServerSocket. port backlog)))
  ([port fun]
     (create-server-aux fun (ServerSocket. port))))

(defn close-server [server]
  (doseq [s @(:connections server)]
    (close-socket s))
  (dosync (ref-set (:connections server) #{}))
  (.close ^ServerSocket (:server-socket server)))

(defn connection-count [server]
  (count @(:connections server)))

(defn parse-input  [input]
  (let [parsed (split input #" ")]
    (case (keyword (first parsed))
      :MSG (format "Þú sendir skilaboð %s.\n" (rest parsed))
      :CD (format "Þú ert kominn í möppu %s.\n" (rest parsed))
      "wat.\n")))

;;; 
;;; HRC
;;; 
;;

(defn handle-client [in out]
  (binding [*in* (BufferedReader. (InputStreamReader. in))
            *out* (OutputStreamWriter. out)]
    (loop []
      (let [input (read-line)]
        (print
         (str input
              (parse-input input)))
        (flush))
      (recur))))

(defn -main []
  (create-server 8091 handle-client -1
                 (. InetAddress getByName "localhost")))
