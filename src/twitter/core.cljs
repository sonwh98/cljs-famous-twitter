(ns ^:figwheel-always twitter.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [com.kaicode.infamous.core :as infamous :refer [events->chan famous]]
            [cljs.core.async :refer [alts!]]))

(enable-console-print!)

(defonce Size (.. famous -components -Size))
(defonce ABSOLUTE (.. Size -ABSOLUTE))
(defonce DEFAULT (.. Size -RELATIVE))

(def sections [{:name "Home" :tweet-number 50}
               {:name "Discover" :tweet-number 50}
               {:name "Connect" :tweet-number 50}
               {:name "Me" :tweet-number 25}])

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
                                   :node/children          (for [{:keys [name tweet-number]} sections]
                                                             {:node/id         (str "section-" name)
                                                              :node/components [{:component/type :DOMElement
                                                                                 :overflow-y     "scroll"
                                                                                 :overflow-x     "hidden"}
                                                                                {:component/type :Align}]
                                                              :node/children   (for [i (range tweet-number)]
                                                                                 {:node/size-mode     [DEFAULT ABSOLUTE]
                                                                                  :node/absolute-size [nil 100]
                                                                                  :node/position      [0 (* 100 i)]
                                                                                  :node/components    [{:component/type  :DOMElement
                                                                                                        :backgroundColor (str "#" (.toString (rand-int 16rFFFFFF) 16))
                                                                                                        :boxingSize      "border-box"
                                                                                                        :lineHeight      "100px"
                                                                                                        :borderBottom    "1px solid black"
                                                                                                        :font-size       "12px"
                                                                                                        :content         (str name " " i)}]
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
                                                               :let [{:keys [name]} (sections i)]]
                                                           {:node/id                name
                                                            :twitter/section-name   name
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
                                                                                      :content        name}]}))}]})



(defn get-unselected-nodes [selected-button-node section-button-nodes]
  (filter #(not= % selected-button-node) section-button-nodes))

(defn get-dom-element-and-align-component [name]
  (let [footer-section-node (infamous/get-node-by-id name)
        section-node (infamous/get-node-by-id (str "section-" name))
        dom-element (infamous/get-famous-component-by-type-name footer-section-node "DOMElement")
        align-component (infamous/get-famous-component-by-type-name section-node "Align")]
    [dom-element align-component]))

(defonce TRANSITION-DURATION 500)

(defn show [name]
  (let [[dom-element align-component] (get-dom-element-and-align-component name)]
    (.. dom-element (removeClass "off") (addClass "on"))
    (.. align-component (set 0 0 0 (clj->js {:duration TRANSITION-DURATION})))))

(defn hide [name]
  (let [[dom-element align-component] (get-dom-element-and-align-component name)]
    (.. dom-element (removeClass "on") (addClass "off"))
    (.. align-component (set 1 0 0 (clj->js {:duration TRANSITION-DURATION})))))

(defn switch-on [name]
  (let [selected-footer-button-node (infamous/get-node-by-id name)
        footer-button-nodes (:node/children (infamous/get-node-by-id "footer"))
        unselected-footer-button-node-ids (map #(:node/id %) (get-unselected-nodes selected-footer-button-node footer-button-nodes))]
    (show name)
    (doseq [id unselected-footer-button-node-ids]
      (hide id))
    ))

(infamous/save scene-graph)
(infamous/render-scene-graph "twitterus")
(switch-on "Home")

(def channels (for [section-button-node (:node/children (infamous/get-node-by-id "footer"))]
                (events->chan section-button-node "tap" (fn [] (:twitter/section-name section-button-node)))))
(go
  (while true
    (let [[section-name channel] (alts! channels)]
      (switch-on section-name))))

