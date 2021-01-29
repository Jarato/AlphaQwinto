package game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class QwinPaper_TEST {

	@Test
	void test_isColorPositionBlocked() {
		QwinPaper paper = new QwinPaper();
		paper.enterNumber(0,1,1);
		paper.enterNumber(0,3,2);
		paper.enterNumber(0,6,5);
		paper.enterNumber(0,8,18);
		
		paper.enterNumber(1,0,1);
		paper.enterNumber(1,5,4);
		
		paper.enterNumber(2,4,1);
		paper.enterNumber(2,6,3);
		paper.enterNumber(2,7,18);
		
		assertTrue(paper.isColorPositionBlocked(0, 0));
		assertTrue(paper.isColorPositionBlocked(0, 1));
		assertTrue(paper.isColorPositionBlocked(0, 3));
		assertTrue(paper.isColorPositionBlocked(0, 4));
		assertFalse(paper.isColorPositionBlocked(0, 5));
		assertTrue(paper.isColorPositionBlocked(0, 6));
		assertFalse(paper.isColorPositionBlocked(0, 7));
		assertTrue(paper.isColorPositionBlocked(0, 8));
		
		assertTrue(paper.isColorPositionBlocked(1, 0));
		assertFalse(paper.isColorPositionBlocked(1, 1));
		assertFalse(paper.isColorPositionBlocked(1, 2));
		assertFalse(paper.isColorPositionBlocked(1, 3));
		assertFalse(paper.isColorPositionBlocked(1, 4));
		assertTrue(paper.isColorPositionBlocked(1, 5));
		assertFalse(paper.isColorPositionBlocked(1, 6));
		assertFalse(paper.isColorPositionBlocked(1, 7));
		assertFalse(paper.isColorPositionBlocked(1, 8));
		
		assertTrue(paper.isColorPositionBlocked(2, 0));
		assertTrue(paper.isColorPositionBlocked(2, 1));
		assertTrue(paper.isColorPositionBlocked(2, 2));
		assertTrue(paper.isColorPositionBlocked(2, 3));
		assertTrue(paper.isColorPositionBlocked(2, 4));
		assertTrue(paper.isColorPositionBlocked(2, 5));
		assertTrue(paper.isColorPositionBlocked(2, 6));
		assertTrue(paper.isColorPositionBlocked(2, 7));
		assertTrue(paper.isColorPositionBlocked(2, 8));
		
		paper = new QwinPaper();
		paper.enterNumber(1, 0, 18);
		
		assertTrue(paper.isColorPositionBlocked(1, 0));
		assertTrue(paper.isColorPositionBlocked(1, 1));
		assertTrue(paper.isColorPositionBlocked(1, 2));
		assertTrue(paper.isColorPositionBlocked(1, 3));
		assertTrue(paper.isColorPositionBlocked(1, 4));
		assertTrue(paper.isColorPositionBlocked(1, 5));
		assertTrue(paper.isColorPositionBlocked(1, 6));
		assertTrue(paper.isColorPositionBlocked(1, 7));
		assertTrue(paper.isColorPositionBlocked(1, 8));
		
		paper = new QwinPaper();
		paper.enterNumber(2, 8, 1);
		
		assertTrue(paper.isColorPositionBlocked(2, 0));
		assertTrue(paper.isColorPositionBlocked(2, 1));
		assertTrue(paper.isColorPositionBlocked(2, 2));
		assertTrue(paper.isColorPositionBlocked(2, 3));
		assertTrue(paper.isColorPositionBlocked(2, 4));
		assertTrue(paper.isColorPositionBlocked(2, 5));
		assertTrue(paper.isColorPositionBlocked(2, 6));
		assertTrue(paper.isColorPositionBlocked(2, 7));
		assertTrue(paper.isColorPositionBlocked(2, 8));
		
		paper = new QwinPaper();
		paper.enterNumber(0, 0, 6);
		paper.enterNumber(0, 5, 7);
		
		paper.enterNumber(1, 5, 6);
		paper.enterNumber(1, 7, 9);
		
		paper.enterNumber(2, 7, 8);
		
		assertTrue(paper.isColorPositionBlocked(0, 0));
		assertTrue(paper.isColorPositionBlocked(0, 1));
		assertTrue(paper.isColorPositionBlocked(0, 2));
		assertTrue(paper.isColorPositionBlocked(0, 3));
		assertTrue(paper.isColorPositionBlocked(0, 4));
		assertTrue(paper.isColorPositionBlocked(0, 5));
		assertFalse(paper.isColorPositionBlocked(0, 6));
		assertFalse(paper.isColorPositionBlocked(0, 7));
		assertFalse(paper.isColorPositionBlocked(0, 8));
		
		assertTrue(paper.isColorPositionBlocked(1, 5));
		assertTrue(paper.isColorPositionBlocked(1, 6));
		assertTrue(paper.isColorPositionBlocked(1, 7));
		
		assertTrue(paper.isColorPositionBlocked(2, 7));
		
		paper = new QwinPaper();
		paper.enterNumber(0, 1, 2);
		paper.enterNumber(0, 3, 4);
		paper.enterNumber(0, 4, 6);

		paper.enterNumber(1, 1, 1);
		paper.enterNumber(1, 3, 3);
		paper.enterNumber(1, 5, 5);
		
		paper.enterNumber(2, 2, 3);
		paper.enterNumber(2, 4, 4);
		paper.enterNumber(2, 7, 7);
		
		assertTrue(paper.isColorPositionBlocked(0, 2));
		assertTrue(paper.isColorPositionBlocked(1, 2));
		assertTrue(paper.isColorPositionBlocked(1, 4));
		assertTrue(paper.isColorPositionBlocked(2, 3));
		assertFalse(paper.isColorPositionBlocked(2, 5));
		assertTrue(paper.isColorPositionBlocked(2, 6));
		
		
	}

}
