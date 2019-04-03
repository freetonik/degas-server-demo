(defproject degas-server "0.1.9"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async  "0.4.474"]
                 [http-kit "2.3.0"]
                 [ring "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [environ "1.0.0"]
                 [compojure "1.6.1"]]
  :main degas-server.core
  :min-lein-version "2.0.0"
  :uberjar-name "degas-server-standalone.jar"
  :profiles {
             :uberjar {:main degas-server.web, :aot :all}
             :production {:env {:production true}}})
