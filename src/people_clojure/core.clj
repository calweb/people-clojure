(ns people-clojure.core
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [compojure.core :as c]
            [ring.adapter.jetty :as j]
            [ring.middleware.params :as p]
            [hiccup.core :as h])
  (:gen-class))

(defn read-people []
  (let [people (slurp "people.csv")
        people (str/split-lines people)
        people (map (fn [line]
                      (str/split line #","))
                    people)
        header (first people)
        people (rest people)
        people (map (fn [line]
                      (apply hash-map (interleave header line)))
                    people)
        people (walk/keywordize-keys people)]
;        people (filter (fn [line]
;                         (= (:country line) country))
;                       people)]
  ;  (spit "filtered_people.edn" (pr-str people))
    people))

(defn countries-html [people]
  (let [all-countries (map :country people)
        unique-countries (set all-countries)
        sorted-countries (sort unique-countries)]
    [:div
     (map (fn [country]
            [:span
              [:a {:href (str "/?country=" country)} country]
              " "])
       sorted-countries)]))

(defn people-html [people]
  [:ol
   (map (fn [person]
          [:li (str (:first_name person) " " (:last_name person))
           [:img {:src "http://www.placecage.com/c/200/200" :alt (str (:first_name person))}]])
            
       people)])

(c/defroutes app
  (c/GET "/" request
   (let [params (:params request)
         country (get params "country")
         ;country (if country country "Brazil")])
         country (or country "United States")
         people (read-people)
         filtered-people (filter (fn [person]
                                   (= (:country person) country))
                           people)]
     (h/html [:html
               [:body
                (countries-html people)  
                (people-html filtered-people)]]))))

(defn -main []
  (j/run-jetty (p/wrap-params app) {:port 3000}))