(ns ^:figwheel-always twitter.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [com.famous.Famous]
            [reagent.core :as reagent]
            [twitter.util :as util :refer [events->chan get-node-by-id render-scene-graph]]
            [cljs.core.async :refer [alts!]]))

(enable-console-print!)

(defonce famous js/famous)
(defonce Size (.. famous -components -Size))

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
                                                         :classes        ["header"]
                                                         :content        "Twitter"}]}

                                  {:node/id                "swapper"
                                   :node/differential-size [nil -200 nil]
                                   :node/position          [0 100]
                                   :node/components        [{:component/type :DOMElement}]
                                   :node/children          (for [{:keys [id]} sections]
                                                             {:node/id         (str "section-" id)
                                                              :node/components [{:component/type :DOMElement
                                                                                 :overflow-y     "scroll"
                                                                                 :overflow-x     "hidden"}
                                                                                {:component/type :Align}]
                                                              :node/children   (for [i (range 12)]
                                                                                 {:node/size-mode     [DEFAULT ABSOLUTE]
                                                                                  :node/absolute-size [nil 100]
                                                                                  :node/position      [0 (* 100 i)]
                                                                                  :node/components    [{:component/type  :DOMElement
                                                                                                        :backgroundColor (str "#" (.toString (rand-int 16rFFFFFF) 16))
                                                                                                        :boxingSize      "border-box"
                                                                                                        :lineHeight      "100px"
                                                                                                        :borderBottom    "1px solid black"
                                                                                                        :font-size       "12px"
                                                                                                        :content         (str "tweet" i)}]
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
                                                           {:node/id                id
                                                            :node/align             [(/ i num-sections)]
                                                            :node/proportional-size [(/ 1 num-sections)]
                                                            :node/components        [{:component/type :DOMElement
                                                                                      :textAlign      "center"
                                                                                      :lineHeight     "100px"
                                                                                      :fontSize       "18px"
                                                                                      :cursor         "pointer"
                                                                                      :classes        (if (= i 0)
                                                                                                        ["navigation" "on"]
                                                                                                        ["navigation" "off"])
                                                                                      :content        id}]}))}]})

(util/save scene-graph)

(render-scene-graph "twitterus")

(def channels (for [section-button-node (:node/children (get-node-by-id "footer"))]
                (events->chan section-button-node "tap" (fn [] (:node/id section-button-node)))))


(defn get-famous-component [famous-node component-name]
  (first (filter (fn [component]
                   (let [cn (.. component -constructor -name)]
                     (= component-name cn)))
                 (.. famous-node getComponents))))

(defn get-unselected-nodes [selected-button-node section-button-nodes]
  (filter #(not= % selected-button-node) section-button-nodes))

(defn show [dom-element align-component]
  (.. dom-element (removeClass "off") (addClass "on"))
  (.. align-component (set 0 0 0 (clj->js {:duration 1500}))))

(defn hide [id]
  (let [footer-section-node (get-node-by-id id)
        section-node (get-node-by-id (str "section-" id))
        dom-element (get-famous-component (:node/famous-node footer-section-node) "DOMElement")
        align-component (get-famous-component (:node/famous-node section-node) "Align")]
    (.. dom-element (removeClass "on") (addClass "off"))
    (.. align-component (set 1 0 0 (clj->js {:duration 1500})))))

(defn switch-on [id]
  (let [section-button-nodes (:node/children (get-node-by-id "footer"))
        selected-button-node (get-node-by-id id)
        selected-button-famous-node (:node/famous-node selected-button-node)
        domElement (get-famous-component selected-button-famous-node "DOMElement")

        selected-section-famous-node (:node/famous-node (get-node-by-id (str "section-" id)))
        align-component (get-famous-component selected-section-famous-node "Align")

        unselected-node-ids (map #(:node/id %) (get-unselected-nodes selected-button-node section-button-nodes))]

    (show domElement align-component)
    (println unselected-node-ids)

    (doseq [id unselected-node-ids]
      (hide id))
    ))

(go
  (while true
    (let [[id channel] (alts! channels)]
      (switch-on id))))
