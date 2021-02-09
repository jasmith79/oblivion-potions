(ns oblivion-potions.csv
  (:require
   [clojure.string :refer [split]]))

;; I'm not pulling in a dep just to parse a simple csv
(defn split-lines
  [txt]
  (split txt #"\n"))

(defn split-row
  [row]
  (split row #",\s?"))

(defn empty-line?
  [line]
  ;; Clojure is smart enough here to discard the truly empty lines,
  ;; so no need to check the length like in the Javascript version.
  ;; However needs the unicode handling for some reason.
  (not (re-find #"(?u),[ \t\xA0\u1680\u180e\u2000-\u200a\u202f\u205f\u3000]*," line)))

(defn parse-csv
  [file-contents]
  (map split-row
       (filter empty-line? 
               (split-lines file-contents))))
