package com.docshifter.core.asposehelper.adapters;

import com.docshifter.core.asposehelper.LicenseHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class DocumentAdapterTest<T extends UnifiedDocument> {
	protected T sut;
	private static final Path NAME = Paths.get("target/test-classes/Test document");

	abstract T createSut(Path path) throws Exception;
	abstract String getExtension();

	@BeforeAll
	static void beforeAll() {
		LicenseHelper.getLicenseHelper();
	}

	@BeforeEach
	void beforeEach() throws Exception {
		sut = createSut(NAME.resolveSibling(NAME.getFileName() + "." + getExtension()));
	}

	@AfterEach
	void afterEach() throws IOException {
		sut.close();
	}

	@Test
	void getPages_returnsCorrectNumberOfPages() {
		assertEquals(9, sut.getPages().count());
	}

	@Test
	void page5_body_hasCorrectContent() {
		assertEquals("""
				3Heading after page breakSuspendisse ut dapibus nulla, eu pretium enim. Nullam tristique fermentum ligula, \
				auctor blandit orci cursus vehicula. Duis ut nisi cursus, pharetra augue vel, euismod enim. Integer justo nisl, \
				consequat vitaedui eget, consequat venenatis enim. Fusce vestibulum accumsan augue, ac finibus augue feugiat in. \
				Curabitur id consectetur nisi, id consequat nunc. Nam rutrum, ipsum eu consectetur tempus, nulla lorem pretium dui, \
				vel sagittis magna nisi at magna. Mauris sed tortor vel quam iaculis ultrices.Ut suscipit congue egestas. Donec \
				maximus at eros ac viverra. Integer vel urna risus. Cras hendrerit sem ac pellentesque venenatis. Duis dictum eu \
				odio sit amet semper. Morbi feugiat quam eu tempus tempor. Integer sitamet blandit augue. Maecenas ornare sapien \
				sed pellentesque commodo. Donec condimentum ex in commodo gravida. Nulla ac eros sagittis, pellentesque mauris \
				tempus, pulvinar purus. Phasellus nulla neque, ultrices id nulla sed, euismod volutpat nulla. Mauris feugiat sit \
				amet diam a venenatis. Fusce ornare purus vel ex euismod, et ultrices tortor dignissim.Maecenas condimentum, massa \
				sit amet laoreet egestas, nisi elit faucibus quam, sed ultricies justo neque vitae sem. Nunc ornare erat nulla, eget \
				pellentesque est mollis nec. In viverra, dui a iaculis sodales, ligula nisl vestibulum eros, vitae condimentum nunc \
				odio ut tortor. Vestibulum sit amet tincidunt magna, in ullamcorper tellus. Sed convallis fringilla urna. Quisque \
				vitae mattis ligula. Nunc ornare ac risus eget mollis. Suspendisse venenatis dignissim nisl, quis volutpat mi suscipit \
				nec. Nam tortor lectus, imperdiet in feugiat sed, elementum tempus ex.3.1ColumnsPellentesque non mollis tellus, sed \
				ullamcorper leo. Donec a eros non urna vulputate posuere tempus vel nisl. Sed congue lorem euismod nibh placerat, sit \
				amet maximus libero laoreet. Vivamus tristique sem pretium semper fringilla. Nunc lacinia, arcu sit amet viverra \
				tristique, risus metus rutrum erat, eget condimentum nisl nisl at nisi. Sed posuere tellus ut tortor aliquam, sed \
				iaculis erat euismod. Morbi ornare convallis nisl eget gravida. Vestibulum ante ipsum primis in faucibus orci luctus \
				et ultrices posuere cubilia curae; Morbi tincidunt velit eu erat maximus pharetra. Vivamus efficitur lectus a nisl \
				aliquet placerat. Morbi convallis posuere sapien at posuere.Aliquam accumsan quis dolor in dignissim. Duis faucibus, \
				ipsum at facilisis tristique, erat ante viverra urna, sagittis commodo lacus magna et ipsum. Praesent dolor mi, laoreet \
				non imperdiet quis, mattis ut urna. Morbi fringilla tincidunt velit ac finibus. Suspendisse posuere velit id mauris \
				viverra, vitae venenatis libero facilisis. Curabitur accumsan lacus a lobortis ultrices. Suspendisse semper mi a est \
				mollis sollicitudin. Proin eleifend, turpis et porta suscipit, odio arcu congue massa, maximus porta dui elit id dui. \
				Phasellus auctor venenatis blandit. Ut sodales euismod quam, sed dapibus nisl facilisis sit amet. Nam congue magna et \
				erat commodo, eget sollicitudin mauris lobortis.Ut suscipit nisl ut tellus ultrices efficitur. Morbi sagittis vestibulum \
				tortor sed pellentesque. In hac habitasse platea dictumst. Sed quam ligula, aliquam nec purus non, laoreet bibendum \
				metus. Curabitur consequat tincidunt egestas. Donec consectetur bibendum felis, sed semper ex porttitor sit amet. \
				Vestibulum maximus ipsum orci, sed laoreet lorem elementum et. Donec ipsum libero, cursus sit amet imperdiet vitae, \
				ultricies eget lacus.Mauris finibus ante nunc, ullamcorper auctor elit pellentesque at. Fusce faucibus nibh sit amet \
				nunc blandit convallis. Nunc venenatis eros eu consectetur hendrerit. Proin faucibus dignissim mi, id\s""",
				sut.getPages().skip(4).findFirst().orElseThrow()
						.getBody()
						.getTextParagraphs()
						.flatMap(RichTextParagraph::getSegments)
						.map(RichTextParagraph.Segment::getContent)
						.collect(Collectors.joining()));
	}

	@Test
	void page5_header_hasCorrectContent() {
		assertEquals("Odd page header section 1",
				sut.getPages().skip(4).findFirst().orElseThrow()
						.getHeader().orElseThrow()
						.getTextParagraphs()
						.flatMap(RichTextParagraph::getSegments)
						.map(RichTextParagraph.Segment::getContent)
						.collect(Collectors.joining()));
	}

	@Test
	void page5_footer_hasCorrectContent() {
		assertEquals("Odd page footer section 1",
				sut.getPages().skip(4).findFirst().orElseThrow()
						.getFooter().orElseThrow()
						.getTextParagraphs()
						.flatMap(RichTextParagraph::getSegments)
						.map(RichTextParagraph.Segment::getContent)
						.collect(Collectors.joining()));
	}

	@Test
	void page1_header_hasImage() {
		Image[] result = sut.getPages().findFirst().orElseThrow()
				.getHeader().orElseThrow()
				.getImages()
				.toArray(Image[]::new);
		assertEquals(1, result.length);
		assertEquals(PageResource.Type.IMAGE, result[0].getType());
		try (com.aspose.imaging.Image img = com.aspose.imaging.Image.load(result[0].getInputStream())) {
			assertEquals(770, img.getWidth());
			assertEquals(207, img.getHeight());
		}
	}

	@Test
	void deletePage() {
		sut.getPages().skip(1).findFirst().orElseThrow().markForDeletion();
		sut.commitDeletes();
		assertEquals(8, sut.getPages().count());
		assertTrue(sut.getPages().skip(1).findFirst().orElseThrow()
				.getBody()
				.getTextParagraphs()
				.map(para -> para.getSegments().map(RichTextParagraph.Segment::getContent).collect(Collectors.joining()))
				.filter(para -> para.contains("Curabitur scelerisque, leo sit amet facilisis condimentum, dui odio posuere ligula"))
				.findAny()
				.isEmpty());
	}

	@Test
	void saveAndReopen() throws Exception {
		Path newPath = NAME.resolveSibling(NAME.getFileName() + " - Saved." + getExtension());
		sut.save(newPath);
		assertTrue(Files.exists(newPath));
		assertNotEquals(0, Files.size(newPath));
		try (UnifiedDocument newDoc = createSut(newPath)) {
			assertEquals(9, newDoc.getPages().count());
		}
	}
}
