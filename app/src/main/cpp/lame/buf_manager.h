//
// Created by 冉高飞 on 2018/4/17.
//

#ifndef AUDIOAPPLICATION_BUF_MANAGER_H
#define AUDIOAPPLICATION_BUF_MANAGER_H

#include <sys/types.h>
#include <stdint.h>
#include <cassert>
#include <cstring>
#include <atomic>
#include <limits>
#include <memory>
#include "saka_log.h"

#ifndef CACHE_ALIGN
#define CACHE_ALIGN 64
#endif

/**
 * 生产者消费者队列
 * @tparam T 队列实体
 */

template<typename T>
class ProducerConsumerQueue {
public:
    //显示构造函数
    explicit ProducerConsumerQueue(int size) : ProducerConsumerQueue(size, new T[size]) {}

    //显示构造函数
    explicit ProducerConsumerQueue(int size, T *buffer) : size_(size), buffer_(buffer) {
        assert(size < std::numeric_limits<int>::max());
    }

    /**
     * 放入queue
     * @param item
     * @return
     */
    bool push(const T &item) {
        return push([&](T *ptr) -> bool {
            *ptr = item;
            return true;
        });
    }

    T getWriteablePtr() {
        T *result = nullptr;

        bool check __attribute__((unused));

        check = push([&](T *head) -> bool {
            result = head;
            return false;
        });
        assert(check == (result != nullptr));

        return result;
    }

    bool commitWriteablePtr(T *ptr) {
        bool result = push([&](T *head) -> bool {
            assert(ptr = head);
            return true;
        });
        return result;
    }

    template<typename F>
    bool push(const F &writer) {
        bool result = false;
        int readptr = read_.load(std::memory_order_acquire);
        int writeptr = write_.load(std::memory_order_relaxed);

        int space = size_ - (int) (writeptr - readptr);
        if (space >= 1) {
            result = true;
            if (writer(buffer_.get() + (writeptr % size_))) {
                ++writeptr;
                write_.store(writeptr, std::memory_order_release);
            }
        }
        return result;
    }


    bool front(T *out_item) {
        return front([&](T *ptr) -> bool {
            *out_item = *ptr;
            return true;
        });
    }

    void pop(void) {
        int readptr = read_.load(std::memory_order_relaxed);
        ++readptr;
        read_.store(readptr, std::memory_order_release);
    }

    template<typename F>
    bool front(const F &reader) {
        bool result = false;

        int writeptr = write_.load(std::memory_order_acquire);
        int readptr = read_.load(std::memory_order_relaxed);

        int available = (int) (writeptr - readptr);
        if (available >= 1) {
            result = true;
            reader(buffer_.get() + (readptr % size_));
        }
        return result;
    }

    uint32_t size(void) {
        int writeptr = write_.load(std::memory_order_acquire);
        int readptr = read_.load(std::memory_order_relaxed);

        return (uint32_t) (writeptr - readptr);
    }

private:
    int size_;//队列容量
    std::unique_ptr<T> buffer_;//唯一指针
    alignas(CACHE_ALIGN) std::atomic<int> read_{0};//64位对齐，这玩意和java中的AtomicInteger类似
    alignas(CACHE_ALIGN) std::atomic<int> write_{0};
};

/**
 * 数据结构体
 *
 */
struct sample_buf {
    uint8_t *buf;//pcm数据的指针
    uint32_t cap;//每一帧数据的大小
    uint32_t size;//实际装入的数据
};

using AudioQueue=ProducerConsumerQueue<sample_buf *>;//将生产消费者队列重命名为AudioQueue

/**
 * 内联函数，释放队列中的sample中的数据指针
 * @param bufs
 * @param count
 */
__inline__ void releaseSampleBufs(sample_buf *bufs, uint32_t &count) {
    if (!bufs || !count) {
        return;
    }
    for (uint32_t i = 0; i < count; i++) {
        if (bufs[i].buf) {
            delete[] bufs[i].buf;
        }
    }
    delete[] bufs;
}

/**
 * 内联函数，为队列分配内存
 * @param count 队列的个数
 * @param sizeInByte，这个最终会向上转换为4的倍数
 * @return
 */
__inline__ sample_buf *allocateSampleBUfs(uint32_t count, uint32_t sizeInByte) {
    if (count <= 0 || sizeInByte <= 0) {
        return nullptr;
    }

    sample_buf *bufs = new sample_buf[count];
    assert(bufs);

    memset(bufs, 0, sizeof(sample_buf) * count);

    uint32_t allocSize = (sizeInByte + 3) & ~3;//这个地方的操作让人难受，不过据说是为了效率，用来补齐到4的倍数
    uint32_t i;
    for (i = 0; i < count; i++) {
        bufs[i].buf = new uint8_t[allocSize];
        if (bufs[i].buf == nullptr) {
            SAKA_LOG_WARN("===Requesting %d buffers,allocated %d in %s", count, i, __FUNCTION__);
            break;
        }
        bufs[i].cap = sizeInByte;
        bufs[i].size = 0;
    }
    if (i < 2) {
        releaseSampleBufs(bufs, i);
        bufs = nullptr;
    }
    count = i;
    return bufs;
}

#endif //AUDIOAPPLICATION_BUF_MANAGER_H
