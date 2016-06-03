/**
 * Copyright (C) Posten Norge AS
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

package no.digipost.android.authentication;

import android.content.Context;
import android.util.Log;

public class TokenEncryption {
    private CryptoAdapter cryptoAdapter;

    public TokenEncryption(final Context context){
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            cryptoAdapter = new KeyStoreAdapter();
        }else{
            cryptoAdapter = new ConcealAdapter(context);
        }
    }

    public boolean isAvailable(){return cryptoAdapter.isAvailable();}
    public String encrypt(String plainText){
        return cryptoAdapter.encrypt(plainText);
    }
    public String decrypt(String cipherText){return cryptoAdapter.decrypt(cipherText);
    }
}
