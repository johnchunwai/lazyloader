package com.jc.lazyloader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LazyLoader<Model, Context> implements Iterable<Model>
{
    public static final int NO_LAZY_LOAD_BATCH_SIZE = 0;

    private final Dao<Model> dao;
    private final MeterManager meterManager;
    private final int lazyLoadBatchSize;

    private final List<Model> models;
    private final Context context;
    private final RateMeter unlazyLoadMeter;
    private final RateMeter lazyLoadLastBatchMeter;
    private final RateMeter lazyLoadBatchMeter;

    private SortOrderKey lastKey;
    private boolean isAllLoaded;

    public LazyLoader(final Context context, final Dao<Model> dao, final int lazyLoadBatchSize,
        final MeterManager meterManager, RateMeter unlazyLoadMeter, RateMeter lazyLoadLastBatchMeter,
        RateMeter lazyLoadBatchMeter) throws Exception
    {
        this.dao = dao;
        this.meterManager = meterManager;
        this.context = context;
        this.lazyLoadBatchSize = lazyLoadBatchSize;

        this.unlazyLoadMeter = unlazyLoadMeter;
        this.lazyLoadLastBatchMeter = lazyLoadLastBatchMeter;
        this.lazyLoadBatchMeter = lazyLoadBatchMeter;

        lastKey = null;
        models = new ArrayList<>();
        models.addAll(loadNextBatch());
    }

    public boolean isEmpty()
    {
        return models == null || models.isEmpty();
    }

    public boolean isAllLoaded()
    {
        return isAllLoaded;
    }

    private List<Model> loadNextBatch() throws Exception
    {
        final List<Model> loadedModels;
        if (lazyLoadBatchSize == NO_LAZY_LOAD_BATCH_SIZE)
        {
            loadedModels = dao.readAll(context);
            isAllLoaded = true;
            meterManager.incrementRateMeter(unlazyLoadMeter, 1L);
        }
        else
        {
            final Dao.ReadModelsResult<Model> result;
            if (lastKey == null)
            {
                result = dao.getModelsFirstBatch(context, lazyLoadBatchSize);
            }
            else
            {
                result = dao.getModelsBatch(context, lastKey, lazyLoadBatchSize);
            }
            lastKey = result.getLastSortKey();
            isAllLoaded = result.isLastBatch();
            RateMeter meter = isAllLoaded ? lazyLoadLastBatchMeter : lazyLoadBatchMeter;
            meterManager.incrementRateMeter(meter, 1L);
            loadedModels = result.getModels();
        }
        return loadedModels;
    }

    @Override
    public Iterator<Model> iterator()
    {
        return new Iterator<Model>()
        {
            private int currIndex = 0;

            @Override
            public boolean hasNext()
            {
                if (currIndex < models.size())
                {
                    return true;
                }
                if (isAllLoaded)
                {
                    return false;
                }
                try
                {
                    final List<Model> loadModels = loadNextBatch();
                    if (loadModels != null && !loadModels.isEmpty())
                    {
                        models.addAll(loadModels);
                    }
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
                return currIndex < models.size();
            }

            @Override
            public Model next()
            {
                return models.get(currIndex++);
            }
        };
    }
}
