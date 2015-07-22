(ns ^:figwheel-always twitter.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [com.famous.Famous]
              [reagent.core :as reagent]
              [twitter.util :as util :refer [events->chan get-node-by-id render-scene-graph]]
              [cljs.core.async :refer [alts!]]))

(enable-console-print!)

(defonce famous js/famous)
(defonce Size (.. famous -components -Size))
(defonce physics (.. famous -physics))
(defonce math (.. famous -math))
(defonce FamousBox (.. physics -Box))
(defonce Spring (.. physics -Spring))
(defonce RotationalSpring (.. physics -RotationalSpring))
(defonce Quaternion (.. math -Quaternion))
(defonce Vec3 (.. math -Vec3))

(defonce ABSOLUTE (.. Size -ABSOLUTE))
(defonce DEFAULT (.. Size -RELATIVE))

(def sections [{:id "Home" :tweet-number 50}
               {:id "Discover" :tweet-number 50}
               {:id "Me" :tweet-number 25}])

(def scene-graph {:node/id "twitterus"
                  :node/children [{:node/id "header"
                                   :node/size-mode [DEFAULT ABSOLUTE]
                                   :node/absolute-size [nil 100]
                                   :node/components [{:component/type :DOMElement
                                                      :fontSize       "30px"
                                                      :lineHeight     "100px"
                                                      :content        "Twitter"}
                                                     ]
                                   }
                                  {:node/id "footer"
                                   :node/size-mode [DEFAULT ABSOLUTE]
                                   :node/absolute-size [nil 100]
                                   :node/align [0 1]
                                   :node/children (let [num-sections (count sections)]
                                                    (for [ i (range num-sections)
                                                            :let [{:keys [id tweet-number]}  (sections i)]]
                                                    {:node/id id
                                                     :node/align [(/ i num-sections)]
                                                     :node/proportional-size [(/ 1 num-sections)]
                                                     :node/components [{:component/type :DOMElement
                                                                        :textAlign "center"
                                                                        :lineHeight "100px"
                                                                        :fontSize "18px"
                                                                        :cursor "pointer"
                                                                        :classes ["navigation"]}]
                                                     }))
                                   }
                                  {:node/id "swapper"
                                   :node/differential-size [nil -200 nil] }
                                  ] })

(util/save scene-graph)

(render-scene-graph "twitterus")
