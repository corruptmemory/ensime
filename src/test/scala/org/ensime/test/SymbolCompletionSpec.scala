package org.ensime.test
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.ensime.test.util.Helpers._


class SymbolCompletionSpec extends Spec with ShouldMatchers{

  describe("Symbol Completion") {

    it("should complete local variable name") {
      withPresCompiler{ cc =>
	val src = srcFile("Test1.scala", contents(
	    "import java.util.Vector",
	    "object Test1{",
	    "def main{",
	    "val dude = 1",
	    "du ",
	    "val horse = 2",
	    "}",
	    "}"
	  ))
	val syms = cc.askCompletionsAt(src.position(4,2))
	syms.completions.exists(s => s.name == "dude") should be(true)
      }
    }

    it("should complete a param name") {
      withPresCompiler{ cc =>
	val src = srcFile("Test1.scala", contents(
	    "import java.util.Vector",
	    "object Test1{",
	    "def main(args:Array[String]){",
	    "val dude = 1",
	    "ar ",
	    "val horse = 1",
	    "}",
	    "}"
	  ))
	val syms = cc.askCompletionsAt(src.position(4,2))
	syms.completions.exists(s => s.name == "args") should be(true)
      }
    }

    it("should complete local variable name, even when we're at the end of method a block'") {
      withPresCompiler{ cc =>
	val src = srcFile("Test1.scala", contents(
	    "import java.util.Vector",
	    "object Test1{",
	    "def main{",
	    "val dude = 1",
	    "du ",
	    "}",
	    "}"
	  ))
	val syms = cc.askCompletionsAt(src.position(4,2))
	expectFailure("I suspect the context does not extend to the closing brace.",
	  "I work around this in Emacs by temporarily inserting '()'",
	  "immediately after the completion point."){()=>
	  syms.completions.exists(s => s.name == "dude") should be(true)
	}

      }
    }

    it("should complete a class imported via wildcard") {
      withPresCompiler{ cc =>
	val src = srcFile("Test1.scala", contents(
	    "import java.util._",
	    "object Test1{",
	    "def main{",
	    "val dude = 1",
	    "Ve ",
	    "val horse = 1",
	    "}",
	    "}"
	  ))
	val syms = cc.askCompletionsAt(src.position(4,1))
	syms.completions.exists(s => s.name == "Vector") should be(true)
      }
    }

    it("should complete a from inside an incomplete context") {
      withPresCompiler{ cc =>
	val src = srcFile("Test1.scala", contents(
	    "trait A {",
	    " val foo = 12",
	    "}",

	    "class B extends A {",
	    " def bar: Unit = {",
	    "fo ",
	    "  "
	  ))
	val syms = cc.askCompletionsAt(src.position(6,1))
	expectFailure("'no context found' exception is expected. Not sure",
	  "yet what's causing the problem."
	){()=>
	  syms.completions.exists(s => s.name == "foo") should be(true)
	}
      }
    }
    
    it("should complete an explicitely imported class-name") {
      withPresCompiler{ cc =>
	val src = srcFile("Test1.scala", contents(
	    "import scala.collection.mutable.HashSet",
	    "import java.util.Vector",
	    "object Test1{",
	    "def main{",
	    "val dude = 1",
	    "Ha ",
	    "val horse = 1",
	    "}",
	    "}"
	  ))
	val syms1 = cc.askCompletionsAt(src.position(4,2))
	syms1.completions.exists(s => s.name == "HashSet") should be(true)
      }
    }


    it("should complete a wildcard imported class-name from source file") {
      withPresCompiler{ cc =>
	val src1 = srcFile("Bar.scala", contents(
	    "package com.bar",
	    "import scala.collection.mutable.HashSet",
	    "import java.util.Vector",
	    "class Bar{",
	    "def main{",
	    "val dude = 1",
	    "  ",
	    "val horse = 1",
	    "}",
	    "}"
	  ))
	val src2 = srcFile("Foo.scala", contents(
	    "package com.foo",
	    "import com.bar._",
	    "class Foo{",
	    "def main{",
	    "val dude = 1",
	    "Ba ",
	    "val horse = 1",
	    "}",
	    "}"
	  ))
	cc.askReloadAndTypeFiles(List(src1, src2))
	val syms = cc.askCompletionsAt(src2.position(4,2))
	syms.completions.exists(s => s.name == "Bar") should be(true)
      }
    }


    it("should complete an explicitely imported class-name from same source file") {
      withPresCompiler{ cc =>
	val src1 = srcFile("Bar.scala", contents(
	    "package com.bar",
	    "class Bar{",
	    "def main{",
	    "val dude = 1",
	    "  ",
	    "val horse = 1",
	    "}",
	    "}"
	  ))
	val src2 = srcFile("Foo.scala", contents(
	    "package com.foo",
	    "import com.bar.Bar",
	    "class Foo{",
	    "def main{",
	    "val dude = 1",
	    "Ba ",
	    "val horse = 1",
	    "}",
	    "}"
	  ))
	cc.askReloadAndTypeFiles(List(src1, src2))
	val syms = cc.askCompletionsAt(src2.position(4,2))
	syms.completions.exists(s => s.name == "Bar") should be(true)
      }
    }



  }


}
