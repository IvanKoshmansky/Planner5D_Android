package com.example.android.planner5d

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load

// TODO: вставить placeholder для загрузки, желательно анимированный

@BindingAdapter("loadFromUrl")
fun ImageView.loadFromUrl(url: String?) {
    if (url != null) {
        // есть иконка
        load(url) {
            // плэйсхолдер показывается во время загрузки
            placeholder(R.drawable.ic_launcher_background)
        }
    } else {
        // иконки нет, оставляем плэйсхолдер
        // важный нюанс: если использовать Coil, то для программного задания картинки из ресурсов
        // надо тоже использовать load
        load(R.drawable.ic_launcher_background)
    }
}
