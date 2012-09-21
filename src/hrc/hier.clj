(defstruct chann :users :subchann :metadata)

(defstruct metadata :name :topic)

(defn create-chann [name]
  (atom
   (struct chann [] {}
           (struct metadata name ""))))

(defn ls-user [chann]
  (:users @chann))

(defn add-user [chann user]
  (if (neg? (.indexOf (:users @chann) user))
    (swap!
     chann
     #(update-in % [:users]
                 conj user))))

(defn add-subchann [chann subchann]
  (swap!
   chann
   #(update-in % [:subchann]
               (partial conj {subchann (create-chann subchann)}))))

(defn change-topic [chann topic]
  (swap!
   chann
   #(update-in % [:metadata :topic]
               (constantly topic))))

(def top-level
  {"prog" (create-chann "prog")
   "etc" (create-chann "etc")
   "home" (create-chann "home")
   "sci" (create-chann "sci")
   "soc" (create-chann "soc")})
