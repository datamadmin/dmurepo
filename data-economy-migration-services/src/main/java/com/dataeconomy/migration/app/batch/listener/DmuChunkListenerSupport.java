package com.dataeconomy.migration.app.batch.listener;

import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DmuChunkListenerSupport extends ChunkListenerSupport {
	@Override
	public void afterChunk(ChunkContext context) {
		log.info("After chunk");
		super.afterChunk(context);
	}

	@Override
	public void beforeChunk(ChunkContext context) {
		log.info("Before chunk");
		super.beforeChunk(context);
	}

	@Override
	public void afterChunkError(ChunkContext context) {
		log.error("chunk error");
		super.afterChunkError(context);
	}

}
