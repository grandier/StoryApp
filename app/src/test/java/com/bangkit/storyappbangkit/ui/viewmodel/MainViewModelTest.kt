package com.bangkit.storyappbangkit.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.bangkit.storyappbangkit.DataDummy
import com.bangkit.storyappbangkit.MainDispatchRule
import com.bangkit.storyappbangkit.data.local.Session
import com.bangkit.storyappbangkit.data.paging.StoryRepository
import com.bangkit.storyappbangkit.data.paging.adapter.StoryAdapter
import com.bangkit.storyappbangkit.data.remote.model.ListStoryItem
import com.bangkit.storyappbangkit.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    companion object {
        private const val TOKEN = "Bearer TOKEN"
    }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainDispatcherRules = MainDispatchRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    val session: Session = mock(Session::class.java)

    @Test
    fun `when there is story data, ensure returned data count is not zero`() = runTest {
        // Given
        val dummyStory = DataDummy.dummyStoryResponse()
        val data: PagingData<ListStoryItem> = PagedTestDataSources.snapshot(dummyStory)
        val story = MutableLiveData<PagingData<ListStoryItem>>()
        story.value = data
        `when`(storyRepository.getStory(TOKEN)).thenReturn(story)

        val mainViewModel = MainViewModel(session, storyRepository)

        // When
        val actualStory: PagingData<ListStoryItem> =
            mainViewModel.getStories(TOKEN).getOrAwaitValue()

        // Then
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStory)

        advanceUntilIdle()

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStory.size, differ.snapshot().size)

        // Memastikan data pertama yang dikembalikan sesuai
        val expectedFirstItem = dummyStory.getOrNull(0)
        val actualFirstItem = differ.snapshot().items.getOrNull(0)
        Assert.assertEquals(expectedFirstItem, actualFirstItem)
    }



    @Test
    fun `when there is no story data, ensure returned data count is zero`() = runTest {
        // Given
        val dummyStory = emptyList<ListStoryItem>()
        val data: PagingData<ListStoryItem> = PagedTestDataSources.snapshot(dummyStory)
        val story = MutableLiveData<PagingData<ListStoryItem>>().apply { value = data }
        `when`(storyRepository.getStory(TOKEN)).thenReturn(story)

        val mainViewModel = MainViewModel(session, storyRepository)

        // When
        val actualStory: PagingData<ListStoryItem> =
            mainViewModel.getStories(TOKEN).getOrAwaitValue()

        // Then
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        advanceUntilIdle()

        // Assertions
        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(0, differ.snapshot().size)
    }

}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}

class PagedTestDataSources private constructor() :
    PagingSource<Int, LiveData<List<ListStoryItem>>>() {
    companion object {
        fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStoryItem>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStoryItem>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}




