To use all modules (without jsprit-examples), make sure you added the following lines to your pom. Just copy and paste it into your pom.

<pre><code>&lt;dependencies&gt; &lt;!-- add this, if you don't have any dependency definitions yet --&gt;
  &#60;dependency&gt;
    &lt;groupId&gt;jsprit&lt;/groupId&gt;
    &lt;artifactId&gt;jsprit-core&lt;/artifactId&gt;
    &lt;version&gt;1.6.2-SNAPSHOT&lt;/version&gt;
  &lt;/dependency&gt;
  &lt;dependency&gt;
    &lt;groupId&gt;jsprit&lt;/groupId&gt;
    &lt;artifactId&gt;jsprit-analysis&lt;/artifactId&gt;
    &lt;version&gt;1.6.2-SNAPSHOT&lt;/version&gt;
  &lt;/dependency&gt;
  &lt;dependency&gt;
    &lt;groupId&gt;jsprit&lt;/groupId&gt;
    &lt;artifactId&gt;jsprit-instances&lt;/artifactId&gt;
    &lt;version&gt;1.6.2-SNAPSHOT&lt;/version&gt;
  &lt;/dependency&gt;
&lt;/dependencies&gt; &lt;!-- add this, if you don't have any dependency definitions yet --&gt;

&lt;repositories&gt; &lt;!-- add this, if you don't have any repository definitions yet --&gt;
  &lt;repository&gt;
    &lt;id&gt;jsprit-snapshots&lt;/id&gt;
    &lt;url&gt;https://github.com/jsprit/mvn-rep/raw/master/snapshots&lt;/url&gt;
  &lt;/repository&gt;
&lt;/repositories&gt; &lt;!-- add this, if you don't have any repository definitions yet --&gt;
</code></pre>
