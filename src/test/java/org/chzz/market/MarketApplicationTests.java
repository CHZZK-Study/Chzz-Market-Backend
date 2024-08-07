package org.chzz.market;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@MockBean(AmazonS3.class)
class MarketApplicationTests {

	@Test
	void contextLoads() {
	}

}
