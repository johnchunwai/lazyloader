package com.jc.lazyloader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LazyLoaderTest
{
    private static class Context
    {
    }

    @Mock
    private Context context;
    @Mock
    private Dao<Integer> dao;
    @Mock
    private MeterManager meterManager;
    @Mock
    private SortOrderKey sortOrderKey;

    private static final int BATCH_SIZE = 5;

    @Test
    void isEmpty_WithNoModels_ShouldReturnTrue() throws Exception
    {
        when(dao.readAll(any())).thenReturn(Collections.emptyList());
        when(dao.getModelsFirstBatch(any(), anyInt())).thenReturn(
            new Dao.ReadModelsResult<>(Collections.emptyList(), null, true));

        LazyLoader<Integer, Context> lazyLoader = new LazyLoader<>(context, dao, BATCH_SIZE, meterManager, null, null,
            null);
        LazyLoader<Integer, Context> unlazyLoader = new LazyLoader<>(context, dao, LazyLoader.NO_LAZY_LOAD_BATCH_SIZE,
            meterManager, null, null, null);

        assertTrue(lazyLoader.isEmpty());
        assertTrue(unlazyLoader.isEmpty());
    }

    @Test
    void isEmpty_WithModels_ShouldReturnFalse() throws Exception
    {
        when(dao.readAll(any())).thenReturn(Collections.singletonList(1));
        when(dao.getModelsFirstBatch(any(), anyInt())).thenReturn(
            new Dao.ReadModelsResult<>(Collections.singletonList(1), sortOrderKey, true));

        LazyLoader<Integer, Context> lazyLoader = new LazyLoader<>(context, dao, BATCH_SIZE, meterManager, null, null,
            null);
        LazyLoader<Integer, Context> unlazyLoader = new LazyLoader<>(context, dao, LazyLoader.NO_LAZY_LOAD_BATCH_SIZE,
            meterManager, null, null, null);

        assertFalse(lazyLoader.isEmpty());
        assertFalse(unlazyLoader.isEmpty());
    }

    @Test
    void isAllLoaded_Initially_ShouldReturnFalse() throws Exception
    {
        when(dao.getModelsFirstBatch(any(), anyInt())).thenReturn(
            new Dao.ReadModelsResult<>(Collections.singletonList(1), sortOrderKey, false));

        LazyLoader<Integer, Context> lazyLoader = new LazyLoader<>(context, dao, BATCH_SIZE, meterManager, null, null,
            null);

        assertFalse(lazyLoader.isAllLoaded());
    }

    @Test
    void isAllLoaded_AfterLoadingAll_ShouldReturnTrue() throws Exception
    {
        when(dao.readAll(any())).thenReturn(Collections.singletonList(1));
        when(dao.getModelsFirstBatch(any(), anyInt())).thenReturn(
            new Dao.ReadModelsResult<>(Collections.singletonList(1), sortOrderKey, true));

        LazyLoader<Integer, Context> lazyLoader = new LazyLoader<>(context, dao, BATCH_SIZE, meterManager, null, null,
            null);
        LazyLoader<Integer, Context> unlazyLoader = new LazyLoader<>(context, dao, LazyLoader.NO_LAZY_LOAD_BATCH_SIZE,
            meterManager, null, null, null);

        assertTrue(lazyLoader.isAllLoaded());
        assertTrue(unlazyLoader.isAllLoaded());
    }

    @Test
    void iterator_HasNext_WithNoModels_ShouldReturnFalse() throws Exception
    {
        when(dao.readAll(any())).thenReturn(Collections.emptyList());
        when(dao.getModelsFirstBatch(any(), anyInt())).thenReturn(
            new Dao.ReadModelsResult<>(Collections.emptyList(), sortOrderKey, true));

        LazyLoader<Integer, Context> lazyLoader = new LazyLoader<>(context, dao, BATCH_SIZE, meterManager, null, null,
            null);
        LazyLoader<Integer, Context> unlazyLoader = new LazyLoader<>(context, dao, LazyLoader.NO_LAZY_LOAD_BATCH_SIZE,
            meterManager, null, null, null);

        assertFalse(lazyLoader.iterator().hasNext());
        assertFalse(unlazyLoader.iterator().hasNext());
    }

    @Test
    void iterator_Next_WithNoModels_ShouldThrowException() throws Exception
    {
        when(dao.readAll(any())).thenReturn(Collections.emptyList());
        when(dao.getModelsFirstBatch(any(), anyInt())).thenReturn(
            new Dao.ReadModelsResult<>(Collections.emptyList(), sortOrderKey, true));

        LazyLoader<Integer, Context> lazyLoader = new LazyLoader<>(context, dao, BATCH_SIZE, meterManager, null, null,
            null);
        LazyLoader<Integer, Context> unlazyLoader = new LazyLoader<>(context, dao, LazyLoader.NO_LAZY_LOAD_BATCH_SIZE,
            meterManager, null, null, null);

        assertThrows(IndexOutOfBoundsException.class, () -> lazyLoader.iterator().next());
        assertThrows(IndexOutOfBoundsException.class, () -> unlazyLoader.iterator().next());
    }

    static class IntSortOrderKey implements SortOrderKey
    {
        private final int value;

        public IntSortOrderKey(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }

        @Override
        public String genWhereClauseForNextBatch()
        {
            return "`col01` > " + value;
        }
    }

    static class IntDao implements Dao<Integer>
    {
        static class Context
        {
            private final boolean doubleResult;

            public Context(boolean doubleResult)
            {
                this.doubleResult = doubleResult;
            }

            public boolean isDoubleResult()
            {
                return doubleResult;
            }
        }

        private final List<Integer> models;

        public IntDao(int elementCount)
        {
            models = new ArrayList<>();
            for (int i = 1; i <= elementCount; i++)
            {
                models.add(i);
            }
        }

        @Override
        public <E> List<Integer> readAll(E context)
        {
            boolean isDoubleResult = false;
            if (context instanceof Context)
            {
                Context doubleContext = (Context) context;
                isDoubleResult = doubleContext.isDoubleResult();
            }
            if (!isDoubleResult)
            {
                return models;
            }
            else
            {
                List<Integer> doubleModels = new ArrayList<>();
                for (Integer model : models)
                {
                    doubleModels.add(model * 2);
                }
                return doubleModels;
            }
        }

        @Override
        public <E> ReadModelsResult<Integer> getModelsFirstBatch(E context, int batchSize)
        {
            return getModelsBatch(context, null, batchSize);
        }

        @Override
        public <E> ReadModelsResult<Integer> getModelsBatch(E context, SortOrderKey prevLastKey, int batchSize)
        {
            int startIndex = 0;
            if (prevLastKey != null)
            {
                IntSortOrderKey prevLastIntKey = (IntSortOrderKey) prevLastKey;
                startIndex = models.indexOf(prevLastIntKey.getValue()) + 1;
            }
            boolean isDoubleResult = false;
            if (context instanceof Context)
            {
                Context doubleContext = (Context) context;
                isDoubleResult = doubleContext.isDoubleResult();
            }
            final List<Integer> subList = models.subList(startIndex, Math.min(startIndex + batchSize, models.size()));
            final List<Integer> resultModels;
            if (isDoubleResult)
            {
                resultModels = new ArrayList<>();
                for (Integer model : subList)
                {
                    resultModels.add(model * 2);
                }
            }
            else
            {
                resultModels = subList;
            }
            final int lastSortKey = subList.get(subList.size() - 1);
            return new ReadModelsResult<>(resultModels, new IntSortOrderKey(lastSortKey),
                lastSortKey == models.get(models.size() - 1));
        }
    }

    @Test
    void iterator_HasNextAndNext_LazyLoadWithModels_ShouldIterateCorrectly() throws Exception
    {
        final int elementCount = 13;
        IntDao intDao = new IntDao(elementCount);
        LazyLoader<Integer, Context> lazyLoader = new LazyLoader<>(context, intDao, BATCH_SIZE, meterManager, null,
            null, null);

        assertFalse(lazyLoader.isAllLoaded());
        assertFalse(lazyLoader.isEmpty());

        int i = 1;
        for (Integer model : lazyLoader)
        {
            assertEquals(i, model);
            i++;
        }
        assertEquals(elementCount, i - 1);
        assertTrue(lazyLoader.isAllLoaded());
        assertFalse(lazyLoader.isEmpty());
    }

    @Test
    void iterator_HasNextAndNext_UnlazyLoadWithModels_ShouldIterateCorrectly() throws Exception
    {
        final int elementCount = 13;
        IntDao intDao = new IntDao(elementCount);
        LazyLoader<Integer, Context> unlazyLoader = new LazyLoader<>(context, intDao,
            LazyLoader.NO_LAZY_LOAD_BATCH_SIZE, meterManager, null, null, null);

        assertTrue(unlazyLoader.isAllLoaded());
        assertFalse(unlazyLoader.isEmpty());

        int i = 1;
        for (Integer model : unlazyLoader)
        {
            assertEquals(i, model);
            i++;
        }
        assertEquals(elementCount, i - 1);
        assertTrue(unlazyLoader.isAllLoaded());
        assertFalse(unlazyLoader.isEmpty());
    }

    @Test
    void iterator_HasNextAndNext_LazyLoadWithModelsWithContext_ShouldIterateCorrectly() throws Exception
    {
        final int elementCount = 13;
        IntDao intDao = new IntDao(elementCount);
        IntDao.Context doubleContext = new IntDao.Context(true);
        LazyLoader<Integer, IntDao.Context> lazyLoader = new LazyLoader<>(doubleContext, intDao, BATCH_SIZE,
            meterManager, null, null, null);

        assertFalse(lazyLoader.isAllLoaded());
        assertFalse(lazyLoader.isEmpty());

        int i = 1;
        for (Integer model : lazyLoader)
        {
            assertEquals(i * 2, model);
            i++;
        }
        assertEquals(elementCount, i - 1);
        assertTrue(lazyLoader.isAllLoaded());
        assertFalse(lazyLoader.isEmpty());
    }

    @Test
    void iterator_HasNextAndNext_unlazyLoadWithModelsWithContext_ShouldIterateCorrectly() throws Exception
    {
        final int elementCount = 13;
        IntDao intDao = new IntDao(elementCount);
        IntDao.Context doubleContext = new IntDao.Context(true);
        LazyLoader<Integer, IntDao.Context> unlazyLoader = new LazyLoader<>(doubleContext, intDao,
            LazyLoader.NO_LAZY_LOAD_BATCH_SIZE, meterManager, null, null, null);

        assertTrue(unlazyLoader.isAllLoaded());
        assertFalse(unlazyLoader.isEmpty());

        int i = 1;
        for (Integer model : unlazyLoader)
        {
            assertEquals(i * 2, model);
            i++;
        }
        assertEquals(elementCount, i - 1);
        assertTrue(unlazyLoader.isAllLoaded());
        assertFalse(unlazyLoader.isEmpty());
    }
}
