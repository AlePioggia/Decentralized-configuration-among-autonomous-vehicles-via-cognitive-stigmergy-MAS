{ include("sample_agent.asl") }

{ include("tester_agent.asl") }

@[test]
+!test_start
    <-  !start;
        !assert_true( started(_,_,_,_,_,_) ).


{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }

//{ include("$moise/asl/org-obedient.asl") }
