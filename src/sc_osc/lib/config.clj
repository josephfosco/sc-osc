;    Copyright (C) 2023 Joseph Fosco. All Rights Reserved
;
;    This program is free software: you can redistribute it and/or modify
;    it under the terms of the GNU General Public License as published by
;    the Free Software Foundation, either version 3 of the License, or
;    (at your option) any later version.
;
;    This program is distributed in the hope that it will be useful,
;    but WITHOUT ANY WARRANTY; without even the implied warranty of
;    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;    GNU General Public License for more details.
;
;    You should have received a copy of the GNU General Public License
;    along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns sc-osc.lib.config)

(def CONFIG {:log-level               :debug
             :sc-default-input-buses  2
             :sc-default-output-buses 2
             :sc-max-audio-buses      1024      ;; default
             :sc-max-control-buses    16384     ;; default
             }
  )

(def LOG-FILE  "./osc.log")

(defn get-config
  [key]
  (get CONFIG key))
