/*
 * Copyright (C) 2016 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.klinker.messenger.adapter;

import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import xyz.klinker.messenger.MessengerRobolectricSuite;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class BlacklistAdapterTest extends MessengerRobolectricSuite {

    private BlacklistAdapter adapter;

    @Mock
    private Cursor cursor;

    @Before
    public void setUp() {
        adapter = new BlacklistAdapter(cursor, null);
    }

    @Test
    public void nullCursorCount() {
        assertEquals(0, new BlacklistAdapter(null, null).getItemCount());
    }

    @Test
    public void cursorCountZero() {
        when(cursor.getCount()).thenReturn(0);
        assertEquals(0, adapter.getItemCount());
    }

}