/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.accounts.fragment;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.accounts.AccountSchema;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.BaseCursorListFragment;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.utils.UIUtils;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class AccountsFragment extends BaseCursorListFragment
{
    public static final String TAG = AccountsFragment.class.getName();

    private List<Long> selectedAccounts = new ArrayList<Long>(1);

    public AccountsFragment()
    {
        emptyListMessageId = R.string.empty_accounts;
        title = R.string.accounts_manage;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        adapter = new AccountCursorAdapter(getActivity(), null, R.layout.sdk_list_row, selectedAccounts);
        lv.setAdapter(adapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStart()
    {
        DisplayUtils.hideLeftTitlePane(getActivity());
        UIUtils.displayTitle(getActivity(), R.string.accounts_manage);
        super.onStart();
    }

    // /////////////////////////////////////////////////////////////
    // ACTIONS
    // ////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        long accountId = cursor.getLong(AccountSchema.COLUMN_ID_ID);

        Boolean hideDetails = false;
        if (!selectedAccounts.isEmpty())
        {
            hideDetails = selectedAccounts.get(0).equals(accountId);
            selectedAccounts.clear();
        }
        l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        l.setItemChecked(position, true);
        l.setSelection(position);
        v.setSelected(true);

        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            selectedAccounts.add(accountId);
        }

        if (hideDetails)
        {
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.removeFragment(getActivity(), DisplayUtils.getCentralFragmentId(getActivity()));
            }
            selectedAccounts.clear();
        }
        else
        {
            displayAccountDetails(accountId);
        }
    }

    public void add()
    {
        AccountTypesFragment newFragment = new AccountTypesFragment();
        FragmentDisplayer.replaceFragment(getActivity(), newFragment, DisplayUtils.getMainPaneId(getActivity()),
                AccountTypesFragment.TAG, true);
        DisplayUtils.switchSingleOrTwo(getActivity(), true);
    }

    public void unselect()
    {
        selectedAccounts.clear();
    }
    
    public void select(long accountId)
    {
        selectedAccounts.clear();
        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            selectedAccounts.add(accountId);
            refreshListView();
            displayAccountDetails(accountId);
        }
    }

    private void displayAccountDetails(long accountId)
    {
        if (getActivity() instanceof MainActivity)
        {
            ((MainActivity) getActivity()).addAccountDetails(accountId);
        }
        DisplayUtils.switchSingleOrTwo(getActivity(), true);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public static void getMenu(Menu menu)
    {
        MenuItem mi;

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_ACCOUNT_ADD, Menu.FIRST + MenuActionItem.MENU_ACCOUNT_ADD,
                R.string.action_add_account);
        mi.setIcon(R.drawable.ic_account_add);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CURSOR ADAPTER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        setListShown(false);
        return new CursorLoader(getActivity(), AccountManager.CONTENT_URI, AccountManager.COLUMN_ALL, null, null, null);
    }
}
