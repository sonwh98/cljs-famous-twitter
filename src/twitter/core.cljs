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
               {:id "Connect" :tweet-number 50}
               {:id "Me" :tweet-number 25}])

(def scene-graph {:node/id       "twitterus"
                  :node/children [{:node/id            "header"
                                   :node/size-mode     [DEFAULT ABSOLUTE]
                                   :node/absolute-size [nil 100]
                                   :node/components    [{:component/type :DOMElement
                                                         :fontSize       "30px"
                                                         :lineHeight     "100px"
                                                         :classes ["header"]
                                                         :content        "Twitter"}]}

                                  {:node/id                "swapper"
                                   :node/differential-size [nil -200 nil]
                                   :node/position          [0 100]
                                   :node/components [{:component/type :DOMElement}]
                                   :node/children          (for [{:keys [id]} sections]
                                                             {:node/id         (str "section-" id)
                                                              :node/components [{:component/type :DOMElement
                                                                                 :overflow-y     "scroll"
                                                                                 :overflow-x     "hidden"}]
                                                              :node/children   (for [i (range 25)]
                                                                                 {:node/size-mode       [DEFAULT ABSOLUTE]
                                                                                  :node/absolute-size   [nil 100]
                                                                                  :node/position        [0 (* 100 i)]
                                                                                  :node/components [{:component/type :DOMElement
                                                                                                     :backgroundColor (str "#" (.toString (rand-int 16rFFFFFF) 16))
                                                                                                     :boxingSize "border-box"
                                                                                                     :lineHeight "100px"
                                                                                                     :borderBottom "1px solid black"
                                                                                                     :font-size "12px"
                                                                                                     :content (str "tweet" i)}]
                                                                                  })})}
                                  {:node/id            "footer"
                                   :node/size-mode     [DEFAULT ABSOLUTE]
                                   :node/absolute-size [nil 100]
                                   :node/align         [0 1]
                                   :node/mount-point   [0 1]
                                   :node/components    [{:component/type :DOMElement
                                                         }]
                                   :node/children      (let [num-sections (count sections)]
                                                         (for [i (range num-sections)
                                                               :let [{:keys [id tweet-number]} (sections i)]]
                                                           {:node/id                (str "footer" id)
                                                            :node/align             [(/ i num-sections)]
                                                            :node/proportional-size [(/ 1 num-sections)]
                                                            :node/components        [{:component/type  :DOMElement
                                                                                      :textAlign       "center"
                                                                                      :lineHeight      "100px"
                                                                                      :fontSize        "18px"
                                                                                      :cursor          "pointer"
                                                                                      :classes         ["navigation"]
                                                                                      :content         id}]}))}]})

(util/save scene-graph)

(render-scene-graph "twitterus")

(def channels (for [{:keys [id]} sections
                    :let [section-node (get-node-by-id  (str "footer" id))]]
                (events->chan section-node "tap" (fn [] (:node/famous-node section-node)))))

(go
  (while true
    (let [[famous-node channel] (alts! channels)
          components (.. famous-node getComponents)]
      (doseq [c components]
        (if (= "DOMElement" (.. c -constructor -name))
          (do
            (println c)
            (.. c (removeClass "off") (addClass "on"))))))))
