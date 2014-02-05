(ns pikarush.export
  (:require [clj-ical.format :as ical]
            [clojure.string :as str])
  (:import org.joda.time.DateTime))

;; export a ical file from the rush events list

(defn map-to-int [v]
  (map (fn [n] (Integer. n))
       v))

(def now "spring2014")
(def events (read-string (slurp (str "events/" now ".edn"))))

(defn ical-events [events]
  (with-out-str
    (ical/write-object
     (vec
      (concat
       [:vcalendar]

       (for [event events]
         (let [year 2014
               [month day]               (map-to-int (str/split (:date event) #"/"))
               [start-hour start-minute] (map-to-int (str/split (nth (:time event) 0) #":"))
               [end-hour end-minute]     (map-to-int (str/split (nth (:time event) 1) #":"))]
             [:vevent
               [:summary (:name event)]
               [:description (:description event)]
               ;; ha, hard coding
               [:dtstart (org.joda.time.DateTime. 2014 month day start-hour start-minute)]
               [:dtend   (org.joda.time.DateTime. 2014 month day end-hour end-minute)]])))))))

(defn make-calendar [events]
  (spit (str "events/cals/" now ".ics")
        (ical-events events)))

(defn wiki-text [events]
  (let [events-by-date (group-by :date events)
        dates (sort (keys events-by-date))]
    (apply str
      (for [date dates]
        (str "=== " date " ===\n"
          (apply str
            (for [event (get events-by-date date)]
              (str "==== " (:name event) " ====\n\n"
                   "'''Time:''' " (first (:time event)) "-" (second (:time event)) "\n\n"
                   "'''Location:''' " (:location event) "\n\n"
                   (:description event) "\n\n"))))))))


(defn -main []
  (make-calendar events)
  (spit "events.mw" (wiki-text events)))

(-main)
