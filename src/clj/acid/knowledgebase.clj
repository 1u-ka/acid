(ns acid.knowledgebase 
  (:require io.fs))

(def
  ^{}
  note!
  (fn [situation context person solution]
    (->> (format
          "# Note
      
for the issue situation:

```
%s
```

# Solution
      
%s

# Details

Project/context: %s

Person: %s"
          situation
          solution
          context
          person)
         (spit
          (io.fs/expandfp
           (str "/knowledgebase-"
                (.format (java.text.SimpleDateFormat. "yy-dd-MM--hh-mm-ss")
                         (java.util.Date.))
                ".md"))))))