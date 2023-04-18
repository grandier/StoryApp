package com.bangkit.storyappbangkit.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bangkit.storyappbangkit.data.remote.api.ApiService
import com.bangkit.storyappbangkit.data.remote.model.ListStoryItem


class StoryPagingSource(private val apiService: ApiService, private val token: String) :
    PagingSource<Int, ListStoryItem>() {
    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData = apiService.getAllStoriesPage(token, position, params.loadSize)
            Log.d("StoryPagingSource", "Response: $responseData")
            Log.d("StoryPagingSource", "Position: $position")
            Log.d("StoryPagingSource", "LoadSize: ${params.loadSize}")
            LoadResult.Page(
                data = responseData.listStory,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (responseData.listStory.isNullOrEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }


    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}



