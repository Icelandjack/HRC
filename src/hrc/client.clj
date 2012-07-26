(ns hrc.client
  (:import (java.net InetAddress Socket SocketException)
           (java.io InputStreamReader OutputStream OutputStreamWriter
                    PrintWriter BufferedReader)))

(defmulti op (fn [cmd & _] (keyword cmd)))

(defmethod op :ping [_] "Fékk PING.")
(defmethod op :default [_] "röng skipun...")

(defn conn-handler [conn]
  (loop []
    (let [msg (.readLine (:in @conn))]
      (println msg)
      (doto (:out @conn)
        (.println (op msg))
        (.flush)))
    (recur)))

(defn connect [server]
  (let [socket (Socket. (:name server) (:port server))
        in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out (PrintWriter. (.getOutputStream socket))
        conn (ref {:in in :out out})]
    (doto (Thread. #(conn-handler conn)) (.start))
    conn))

(defn -main []
  (connect {:name "localhost" :port 8989}))