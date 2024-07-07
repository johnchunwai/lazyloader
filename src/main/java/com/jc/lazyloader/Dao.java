package com.jc.lazyloader;

import java.util.List;

public interface Dao<Model>
{
    class ReadModelsResult<Model>
    {
        public final List<Model> models;
        public final SortOrderKey lastSortKey;
        public final boolean isLastBatch;

        public ReadModelsResult(List<Model> models, SortOrderKey lastSortKey, boolean isLastBatch)
        {
            this.models = models;
            this.lastSortKey = lastSortKey;
            this.isLastBatch = isLastBatch;
        }

        public List<Model> getModels()
        {
            return models;
        }

        public SortOrderKey getLastSortKey()
        {
            return lastSortKey;
        }

        public boolean isLastBatch()
        {
            return isLastBatch;
        }
    }

    <Context> List<Model> readAll(Context context) throws Exception;

    <Context> ReadModelsResult<Model> getModelsFirstBatch(Context context, int batchSize) throws Exception;
    <Context> ReadModelsResult<Model> getModelsBatch(Context context, SortOrderKey prevLastKey, int batchSize) throws Exception;
}
