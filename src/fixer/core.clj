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
  (def a (map #(assoc (parse-string (fetch-url (str apiurl %))) "ID" %) 
              (map 
                #(.substring % 4 26)
                (into [] (.split (slurp "stiftelsenls") "\n"))))))

(-main)

(def b (map #(merge { 
               "track" (get-in % ["track" "name"])
               "nr" (get-in % ["track" "track-number"])
               "artist" (get-in % ["track" "artists" 0 "name"])
               "album" (get-in % ["track" "album" "name"])
               "year" (get-in % ["track" "album" "released"])
               "ID" (get-in % ["ID"])
               } {})
            a))

(def tags (map #(vec (list
                  (str "TITLE=" (% "track") "\nALBUM=" (% "album") "\nTRACKNUMBER=" (% "nr") "\nYEAR=" (% "year") "\n")
                  (str "tags" (% "ID"))
                  )) b)
                  )

;Dump files to disk, one tags file per song
#_(doseq [x tags]
  (spit (second x) (first x)))

;ISO-8859-1 !
#_(doseq [x tags]
  (with-open [wrt (java.io.PrintWriter. (second x) "ISO-8859-1")]
    (println (first x))
    (.println wrt (first x))))

(def cmds (map #(str "vorbiscomment -w -c tags" (% "ID") " dump" (% "ID") "\n") b))
;Write command file
#_(spit "tagscommands" (reduce str cmds))

(def encodecmds (map #(str "sox dump" (% "ID") " -C -6.0 out" (% "ID") ".mp3\n") b))
;Write command file
#_(spit "encodecommands" "")
#_(doseq [x encodecmds]
  (spit "encodecommands" x :append true))

(defn r[title from to]
  (.replace title from to))

;Also, period at end of name obviously will not work... TODO
(defn fixname [title]
  (->
;    (r title " " "_")
    (r title "/" "_")
    (r "?" "_")
    (r "<" "_")
    (r ">" "_")
    (r "\\" "_")
    (r ":" "_")
    (r "|" "_")
    (r "\"" "_")
    (r "^" "_")
    ))

(def renamecmds (map #(str "mv out" (% "ID") ".mp3 \"" (fixname (% "artist")) "-" (fixname (% "track")) ".mp3\"\n") b))
;Write command file
#_(spit "renamecommands" (reduce str renamecmds))

