(ns acid.knowledgebase 
  (:require io.fs))

(def
  ^{:notes ["move to exocortex domain"]}
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
                (.format (java.text.SimpleDateFormat. "yy-MM-dd--hh-mm-ss")
                         (java.util.Date.))
                ".md"))))))