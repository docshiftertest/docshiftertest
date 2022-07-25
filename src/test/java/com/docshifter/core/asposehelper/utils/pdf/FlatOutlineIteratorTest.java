package com.docshifter.core.asposehelper.utils.pdf;

import com.aspose.pdf.Document;
import com.aspose.pdf.OutlineItemCollection;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class FlatOutlineIteratorTest {
	@Test
	void iterateOverRoot() {
		try (Document doc = new Document("target/test-classes/5.4-LASTNAME-FIRSTNAMEYINITIALYYYY-000001763.pdf")) {
			String[] bookmarks = StreamSupport.stream(FlatOutlineIterator.createIterable(doc.getOutlines()).spliterator(),
							false)
					.map(OutlineItemCollection::getTitle)
					.toArray(String[]::new);
			assertArrayEquals(new String[] {
					"1 Criminalization of Music Piracy is misguided (refute)",
					"1.1 Entertainment",
					"1.2 Musical Production",
					"1.2.1 Not without risk",
					"1.2.1.1 Intellectual property",
					"1.2.1.2 Another level 4",
					"1.2.1.3 And another level 4",
					"1.2.1.3.1 A new level 5",
					"1.2.1.3.2 And another level 5",
					"1.2.2 Emotion",
					"1.2.2.1 Creativity",
					"1.3 Entertainment",
					"1.4 Musical Production",
					"1.4.1 Not without risk",
					"1.4.1.1 Intellectual property",
					"2 References"
			}, bookmarks);
		}
	}

	@Test
	void iterateOverChild() {
		try (Document doc = new Document("target/test-classes/5.4-LASTNAME-FIRSTNAMEYINITIALYYYY-000001763.pdf")) {
			OutlineItemCollection child = doc.getOutlines().get_Item(1).get_Item(2).get_Item(1);
			String[] bookmarks = StreamSupport.stream(FlatOutlineIterator.createIterable(child).spliterator(),
							false)
					.map(OutlineItemCollection::getTitle)
					.toArray(String[]::new);
			assertArrayEquals(new String[] {
					"1.2.1 Not without risk",
					"1.2.1.1 Intellectual property",
					"1.2.1.2 Another level 4",
					"1.2.1.3 And another level 4",
					"1.2.1.3.1 A new level 5",
					"1.2.1.3.2 And another level 5"
			}, bookmarks);
		}
	}

	@Test
	void removeSomeBookmarks() {
		final Set<String> titlesToRemove = Set.of("1.2.1.3.1 A new level 5", "1.2.2 Emotion", "2 References");
		try (Document doc = new Document("target/test-classes/5.4-LASTNAME-FIRSTNAMEYINITIALYYYY-000001763.pdf")) {
			for (Iterator<OutlineItemCollection> it = new FlatOutlineIterator(doc.getOutlines()); it.hasNext();) {
				OutlineItemCollection oic = it.next();
				if (titlesToRemove.contains(oic.getTitle())) {
					it.remove();
				}
			}
			String[] bookmarks = StreamSupport.stream(FlatOutlineIterator.createIterable(doc.getOutlines()).spliterator(),
							false)
					.map(OutlineItemCollection::getTitle)
					.toArray(String[]::new);
			assertArrayEquals(new String[] {
					"1 Criminalization of Music Piracy is misguided (refute)",
					"1.1 Entertainment",
					"1.2 Musical Production",
					"1.2.1 Not without risk",
					"1.2.1.1 Intellectual property",
					"1.2.1.2 Another level 4",
					"1.2.1.3 And another level 4",
					"1.2.1.3.2 And another level 5",
					"1.3 Entertainment",
					"1.4 Musical Production",
					"1.4.1 Not without risk",
					"1.4.1.1 Intellectual property"
			}, bookmarks);
		}
	}
}
