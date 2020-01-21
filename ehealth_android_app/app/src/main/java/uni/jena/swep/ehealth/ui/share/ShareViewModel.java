package uni.jena.swep.ehealth.ui.share;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ShareViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ShareViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Here will be options for sharing data maybe.");
    }

    public LiveData<String> getText() {
        return mText;
    }
}