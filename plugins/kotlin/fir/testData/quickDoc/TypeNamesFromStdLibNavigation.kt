class A {

}

fun foo(x : A) { }

fun main(args: Array<String>) {
    <caret>foo()
}

//INFO: <div class='definition'><pre>fun foo(x: A): Unit</pre></div></pre></div><table class='sections'><p></table><div class='bottom'><icon src="file"/>&nbsp;TypeNamesFromStdLibNavigation.kt<br/></div>
