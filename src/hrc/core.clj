(ns hrc.core
  (:import
   (java.net Socket ServerSocket SocketException)
   (java.io PrintWriter InputStreamReader BufferedReader)))

(defn accept-connection [server-socket]
  (try (.accept server-socket)
       (catch SocketException e)))

(defn start-server
  "A chat server accepting requests."
  [port]
  (with-open
      [s-socket (ServerSocket. port)
       conn (accept-connection s-socket)
       ;; Input from client
       in (BufferedReader.
           (InputStreamReader.
            (.getInputStream conn)))
       ;; Writing to client
       out (PrintWriter. (.getOutputStream conn) true)]
    ;; /dev/stdout becomes client
    (loop []
        (binding [*out* out]
          (println "Testing:" (.readLine in)))
        (recur))))

(defn -main []
  (print "Hello, World!"))