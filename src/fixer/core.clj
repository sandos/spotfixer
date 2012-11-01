(ns fixer.core
  (:use [cheshire.core]))

(import '(java.net URL)
        '(java.lang StringBuilder)
        '(java.io BufferedReader InputStreamReader))

(defn fetch-url
  "Return the web page as a string."
  [address]
  (let [url (URL. address)]
    (with-open [stream (. url (openStream))]
      (let [buf (BufferedReader. (InputStreamReader. stream))]
        (apply str (line-seq buf))))))

(def apiurl "http://ws.spotify.com/lookup/1/.json?uri=spotify:track:")

(defn -main
  "I don't do a whole lot."
  [& args]
  (def a (map #(parse-string (fetch-url (str apiurl %))) 
              (map 
                #(.substring % 4 26)
                (into [] (.split (slurp "names") "\n"))
                )))

  )

(-main)



(doseq [x a]
  (prn (get-in x ["track" "name"])
       (get-in x ["track" "track-number"])
       (get-in x ["track" "artists" 0 "name"])
       (get-in x ["track" "album" "name"])
       (get-in x ["track" "album" "released"])
       ))

