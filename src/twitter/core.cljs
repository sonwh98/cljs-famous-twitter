(ns ^:figwheel-always twitter.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [com.kaicode.infamous :as infamous :refer [events->chan famous]]
            [cljs.core.async :refer [alts!]]))

(enable-console-print!)

(defonce Size (.. famous -components -Size))
(defonce NODE (.. famous -core -Node))
(defonce ABSOLUTE (.. Size -ABSOLUTE))
(defonce DEFAULT (.. Size -RELATIVE))
(defonce RENDER_SIZE (.. NODE -RENDER_SIZE))

(def scene-graph {:node/id       "twitterus"
                  :node/children [{:node/id            "header"
                                   :node/size-mode     [DEFAULT ABSOLUTE]
                                   :node/absolute-size [nil 100]
                                   :node/components    [{:component/type :DOMElement
                                                         :fontSize       "30px"
                                                         :lineHeight     "100px"
                                                         :classes        ["header"]
                                                         :content        "Authentication"}]}
                                  {:node/id         "login-form"
                                   :node/align      [0 0.1]
                                   :node/components [{:component/type :DOMElement
                                                      :overflow-y     "hidden"
                                                      :overflow-x     "hidden"}
                                                     {:component/type :Align}]
                                   :node/children   [{:node/size-mode   [RENDER_SIZE RENDER_SIZE]
                                                      :node/align       [0.5 0.1]
                                                      :node/mount-point [1 0]
                                                      :node/components  [{:component/type :DOMElement
                                                                          :boxingSize     "border-box"
                                                                          :borderBottom   "1px solid black"
                                                                          :font-size      "20px"
                                                                          :content        "Email"}]
                                                      }

                                                     {:node/size-mode   [RENDER_SIZE RENDER_SIZE]
                                                      :node/align       [0.5 0.1]
                                                      :node/mount-point [0 0]
                                                      :node/components  [{:component/type :DOMElement
                                                                          :borderBottom   "1px solid black"
                                                                          :font-size      "12px"
                                                                          :content        "<input id='email' type='text' />"}]
                                                      }


                                                     {:node/size-mode   [RENDER_SIZE RENDER_SIZE]
                                                      :node/align       [0.5 .15]
                                                      :node/mount-point [1 0]
                                                      :node/components  [{:component/type :DOMElement
                                                                          :borderBottom   "1px solid black"
                                                                          :font-size      "20px"
                                                                          :content        "Password"}]
                                                      }
                                                     {:node/size-mode   [RENDER_SIZE RENDER_SIZE]
                                                      :node/align       [0.5 .15]
                                                      :node/mount-point [0 0]
                                                      :node/components  [{:component/type :DOMElement
                                                                          :boxingSize     "border-box"
                                                                          :borderBottom   "1px solid black"
                                                                          :content        "<input id='password' type='password' />"}]
                                                      }

                                                     {:node/id            "login-button"
                                                      :node/size-mode     [ABSOLUTE RENDER_SIZE]
                                                      :node/absolute-size [60]
                                                      :node/align         [0.5 .18]
                                                      :node/mount-point   [0 0]
                                                      :node/components    [{:component/type :DOMElement
                                                                            :content        "<button>Log in </button>"}]
                                                      }
                                                     ]}
                                  ]})

(infamous/render-scene-graph scene-graph "body")

(def login-clicks (events->chan (infamous/get-node-by-id "login-button") "tap" #(identity "login")))

(go (while true
      (println (<! login-clicks))
      (let [email (.. js/document (getElementById "email") -value)
            password (.. js/document (getElementById "password") -value)]
        (println email " password=" password))
      ))