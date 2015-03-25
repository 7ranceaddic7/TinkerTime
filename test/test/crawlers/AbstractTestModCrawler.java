package test.crawlers;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import test.util.TestModLoader;
import test.util.ModStubs;
import aohara.tinkertime.crawlers.CrawlerFactory.UnsupportedHostException;
import aohara.tinkertime.models.Mod;

public abstract class AbstractTestModCrawler {
	
	protected void compare(
		ModStubs stub, String id, Date updatedOn, String creator,
		String newestFile, String downloadLink, String imageLink, String supportedVersion
	) throws IOException, UnsupportedHostException {
		Mod actualMod = TestModLoader.loadMod(stub);
		
		Mod expectedMod = new Mod(
			id,
			stub.name,
			newestFile,
			creator,
			imageLink != null ? new URL(imageLink) : null,
			stub.url,
			updatedOn,
			supportedVersion
		);
		assertEquals(expectedMod.getName(), actualMod.getName());
		assertEquals(expectedMod.getNewestFileName(), actualMod.getNewestFileName());
		assertEquals(expectedMod.getCreator(), actualMod.getCreator());
		assertEquals(expectedMod.getImageUrl(), actualMod.getImageUrl());
		assertEquals(expectedMod.getPageUrl(), actualMod.getPageUrl());

		Calendar expectedDate = Calendar.getInstance();
		expectedDate.setTime(expectedMod.getUpdatedOn());
		
		Calendar actualDate = Calendar.getInstance();
		actualDate.setTime(actualMod.getUpdatedOn());
		
		assertEquals(expectedDate.get(Calendar.YEAR), actualDate.get(Calendar.YEAR));
		assertEquals(expectedDate.get(Calendar.MONTH), actualDate.get(Calendar.MONTH));
		assertEquals(expectedDate.get(Calendar.DATE), actualDate.get(Calendar.DATE));
	}
	
	protected Date getDate(int year, int month, int date){
		Calendar c = Calendar.getInstance();
		c.set(year, month, date, 0, 0, 0);
		return c.getTime();
	}

}