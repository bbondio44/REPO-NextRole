package com.nextrole.matching;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ScoringTest {
 @Test void noInformationMeansUnknownNotZero(){assertNull(Scoring.affinity(null,null));}
 @Test void renormalizeMissingEvidence(){assertEquals(80,Scoring.affinity(null,80));}
 @Test void combineInterestAndSkills(){assertEquals(69,Scoring.affinity(90,35));}
 @Test void avoidFalseConfidenceWithSparseAnswers(){assertEquals(14,Scoring.confidence(20,false));}
 @Test void emptyDenominatorIsUnknown(){assertNull(Scoring.percent(0,0));}
}
