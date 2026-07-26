import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import * as linksApi from '@/api/linksApi';

export const fetchMyLinks = createAsyncThunk('links/fetchMyLinks', async (params) => {
  return linksApi.listMyLinks(params);
});

export const createLinkThunk = createAsyncThunk(
  'links/create',
  async (payload, { rejectWithValue }) => {
    try {
      return await linksApi.createLink(payload);
    } catch (err) {
      // createAsyncThunk normally serializes rejected errors down to just a
      // message, dropping axios's err.response.data (which has the real
      // fieldErrors from GlobalExceptionHandler). rejectWithValue preserves
      // it so the UI can show the actual validation message, not a generic one.
      return rejectWithValue(err.response?.data);
    }
  }
);

export const deleteLinkThunk = createAsyncThunk('links/delete', async (linkId) => {
  await linksApi.deleteLink(linkId);
  return linkId;
});

const linksSlice = createSlice({
  name: 'links',
  initialState: {
    items: [],
    totalElements: 0,
    totalPages: 0,
    page: 0,
    status: 'idle', // 'idle' | 'loading' | 'succeeded' | 'failed'
    error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchMyLinks.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(fetchMyLinks.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.items = action.payload.content;
        state.totalElements = action.payload.totalElements;
        state.totalPages = action.payload.totalPages;
        state.page = action.payload.number;
      })
      .addCase(fetchMyLinks.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.error.message;
      })
      .addCase(createLinkThunk.fulfilled, (state, action) => {
        // Prepend so the newest link shows first without waiting for a refetch.
        state.items.unshift(action.payload);
        state.totalElements += 1;
      })
      .addCase(deleteLinkThunk.fulfilled, (state, action) => {
        state.items = state.items.filter((link) => link.id !== action.payload);
        state.totalElements -= 1;
      });
  },
});

export default linksSlice.reducer;