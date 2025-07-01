package edu.hm.cs.kreisel_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KreiselBackendApplicationTest {

	@Test
	void contextLoads() {
		// Dieser Test stellt sicher, dass der Spring-Kontext erfolgreich geladen werden kann
		// Dies deckt indirekt die main-Methode ab
	}

	@Test
	void applicationStarts() {
		// Dieser Test ruft explizit die main-Methode auf
		// In der Praxis würde man dies normalerweise vermeiden, da es die Anwendung startet
		// Dies ist nur für die Code-Coverage
		KreiselBackendApplication.main(new String[]{});
	}
}