package com.nextrole.skillgap;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GapPolicyTest {
 @Test void noEvaluationIsNotZero(){assertNull(GapPolicy.amount(null,3));assertEquals("no_evaluada",GapPolicy.status(null,3));assertEquals("pendiente",GapPolicy.priority(null,3,true));}
 @Test void zeroKnowledgeIsMissing(){assertEquals(3,GapPolicy.amount(0,3));assertEquals("faltante",GapPolicy.status(0,3));assertEquals("alta",GapPolicy.priority(0,3,true));}
 @Test void partialAndCovered(){assertEquals("parcial",GapPolicy.status(1,3));assertEquals("cubierta",GapPolicy.status(4,3));assertEquals(0,GapPolicy.amount(4,3));}
}
