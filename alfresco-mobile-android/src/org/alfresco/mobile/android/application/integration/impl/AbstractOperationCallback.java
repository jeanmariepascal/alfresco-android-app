package org.alfresco.mobile.android.application.integration.impl;

import org.alfresco.mobile.android.application.integration.Operation;
import org.alfresco.mobile.android.application.integration.OperationGroupResult;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.OperationService.BatchOperationCallBack;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.NotificationHelper;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public abstract class AbstractOperationCallback<T> implements Operation.OperationCallBack<T>, BatchOperationCallBack
{
    protected Context context;

    protected int totalItems;

    protected int pendingItems;

    protected String inProgress;

    protected String complete;

    protected int notificationVisibility;

    public AbstractOperationCallback(Context context, int totalItems, int pendingItems)
    {
        this.context = context;
        this.totalItems = totalItems;
        this.pendingItems = pendingItems;
    }

    @Override
    public void onPreExecute(Operation<T> task)
    {
        notificationVisibility = ((AbstractOperationRequestImpl) task.getOperationRequest())
                .getNotificationVisibility();

        if (task.getStartBroadCastIntent() != null)
        {
            LocalBroadcastManager.getInstance(context).sendBroadcast(task.getStartBroadCastIntent());
        }

        switch (notificationVisibility)
        {
            case OperationRequest.VISIBILITY_NOTIFICATIONS:
                NotificationHelper.createIndeterminateNotification(getBaseContext(), task.getOperationRequest()
                        .getNotificationTitle(), inProgress, totalItems - pendingItems + "/" + totalItems);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPostExecute(Operation<T> task, T results)
    {
        if (task.getCompleteBroadCastIntent() != null)
        {
            LocalBroadcastManager.getInstance(context).sendBroadcast(task.getCompleteBroadCastIntent());
        }

        switch (notificationVisibility)
        {
            case OperationRequest.VISIBILITY_NOTIFICATIONS:
                NotificationHelper.createIndeterminateNotification(getBaseContext(), task.getOperationRequest()
                        .getNotificationTitle(), complete, totalItems - pendingItems + "/" + totalItems);
                break;
            default:
                break;
        }
    }

    @Override
    public void onProgressUpdate(Operation<T> task, Long values)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onError(Operation<T> task, Exception e)
    {
        // TODO Auto-generated method stub
    }

    protected Context getBaseContext()
    {
        return context;
    }

    @Override
    public void onPostBatchExecution(OperationGroupResult result)
    {
        if (!result.failedRequest.isEmpty()){
            //TODO ERROR Case
        }
        
        switch (result.notificationVisibility)
        {
            case OperationRequest.VISIBILITY_NOTIFICATIONS:
                Bundle b = new Bundle();
                b.putString(NotificationHelper.ARGUMENT_TITLE, complete);
                NotificationHelper.createNotification(getBaseContext(), b);
                break;
            case OperationRequest.VISIBILITY_DIALOG:
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(IntentIntegrator.ACTION_OPERATIONS_COMPLETE);
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
                removeOperationUri(result);
                break;
            case OperationRequest.VISIBILITY_TOAST:
                MessengerManager.showToast(getBaseContext(), "Operation complete");
                removeOperationUri(result);
                break;
            default:
                removeOperationUri(result);
                break;
        }
    }

    protected void removeOperationUri(OperationGroupResult result)
    {
        Uri operationUri = null;
        for (OperationRequest operationRequest : result.completeRequest)
        {
            operationUri = ((AbstractOperationRequestImpl) operationRequest).getNotificationUri();
            context.getContentResolver().delete(operationUri, null, null);
        }
        
        for (OperationRequest operationRequest : result.failedRequest)
        {
            operationUri = ((AbstractOperationRequestImpl) operationRequest).getNotificationUri();
            context.getContentResolver().delete(operationUri, null, null);
        }
    }
}
